package com.baize.flux.framework.channel;

import com.baize.flux.api.source.RecordBatch;
import com.baize.flux.api.source.RecordSizeEstimator;
import com.baize.flux.framework.metrics.ChannelMetrics;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** In-memory local channel with atomic batch, record and byte backpressure. */
public final class LocalDataChannel<T> implements DataChannel<T> {
    private final String channelId; private final int maxBatches, expectedProducers;
    private final long maxRecords, maxBytes, maxRecordsPerSecond, maxBytesPerSecond;
    private final Deque<Entry<T>> buffer = new ArrayDeque<Entry<T>>();
    private final ReentrantLock lock = new ReentrantLock(); private final Condition notEmpty=lock.newCondition(), notFull=lock.newCondition();
    private final ChannelMetrics metrics; private int createdWriters, remainingProducers; private long bufferedRecords, bufferedBytes, nextRecordPermitNanos, nextBytePermitNanos;
    private boolean readerOpened, cancelled; private Throwable failure;
    public LocalDataChannel(String channelId, int capacity, int expectedProducers) { this(channelId, capacity, 0, 0, 0, 0, expectedProducers); }
    public LocalDataChannel(String channelId, int maxBatches, long maxRecords, long maxBytes, long maxRecordsPerSecond, long maxBytesPerSecond, int expectedProducers) {
        if (maxBatches<=0 && maxRecords<=0 && maxBytes<=0) throw new IllegalArgumentException("at least one buffer limit must be greater than 0");
        if (expectedProducers<=0 || maxRecordsPerSecond<0 || maxBytesPerSecond<0) throw new IllegalArgumentException("invalid channel limits");
        this.channelId=Objects.requireNonNull(channelId,"channelId must not be null"); this.maxBatches=maxBatches; this.maxRecords=maxRecords; this.maxBytes=maxBytes; this.maxRecordsPerSecond=maxRecordsPerSecond; this.maxBytesPerSecond=maxBytesPerSecond; this.expectedProducers=expectedProducers; remainingProducers=expectedProducers; metrics=new ChannelMetrics(channelId);
    }
    public ChannelWriter<T> openWriter(){ lock.lock(); try { ensureActive(); if(createdWriters>=expectedProducers) throw new IllegalStateException("Too many writers created for channel '"+channelId+"'"); createdWriters++; return new LocalChannelWriter(); } finally {lock.unlock();} }
    public ChannelReader<T> openReader(){lock.lock();try {if(readerOpened) throw new IllegalStateException("Channel '"+channelId+"' only supports one reader");readerOpened=true;return new LocalChannelReader();}finally{lock.unlock();}}
    public void fail(Throwable cause){Objects.requireNonNull(cause,"cause must not be null");lock.lock();try{if(failure==null)failure=cause;signalAll();}finally{lock.unlock();}}
    public void cancel(){lock.lock();try{cancelled=true;signalAll();}finally{lock.unlock();}}
    public ChannelMetrics getMetrics(){return metrics;} public void close(){cancel();}
    private void writeInternal(T value) throws Exception { Objects.requireNonNull(value,"channel value must not be null"); Entry<T> entry=entry(value); lock.lockInterruptibly(); try {
        boolean oversized=exceeds(entry) && buffer.isEmpty();
        while (!canAccept(entry) && !oversized && failure==null && !cancelled) { long start=System.nanoTime(); try{notFull.await();}finally{metrics.addWriteBlockedNanos(System.nanoTime()-start);} oversized=exceeds(entry)&&buffer.isEmpty(); }
        ensureActive(); if(oversized) metrics.recordOversizedBatch(); awaitRate(entry); ensureActive();
        buffer.addLast(entry); bufferedRecords+=entry.records; bufferedBytes+=entry.bytes; metrics.recordEnqueued(buffer.size(), bufferedRecords, bufferedBytes); notEmpty.signal();
    } finally {lock.unlock();} }
    private void awaitRate(Entry<T> entry) throws InterruptedException { long now=System.nanoTime(); long due=Math.max(nextRecordPermitNanos,nextBytePermitNanos); while(due>now){ long start=System.nanoTime(); notFull.awaitNanos(due-now); metrics.addRateLimitWaitNanos(System.nanoTime()-start); ensureActive(); now=System.nanoTime(); due=Math.max(nextRecordPermitNanos,nextBytePermitNanos); }
        now=System.nanoTime(); if(maxRecordsPerSecond>0) nextRecordPermitNanos=Math.max(now,nextRecordPermitNanos)+nanosFor(entry.records,maxRecordsPerSecond); if(maxBytesPerSecond>0) nextBytePermitNanos=Math.max(now,nextBytePermitNanos)+nanosFor(entry.bytes,maxBytesPerSecond); }
    private static long nanosFor(long amount,long perSecond){ if(amount<=0)return 0; return amount>Long.MAX_VALUE/1_000_000_000L ? Long.MAX_VALUE : Math.max(1L, amount*1_000_000_000L/perSecond); }
    private boolean canAccept(Entry<T> e){return (maxBatches<=0||buffer.size()+1<=maxBatches)&&(maxRecords<=0||bufferedRecords+e.records<=maxRecords)&&(maxBytes<=0||bufferedBytes+e.bytes<=maxBytes);}
    private boolean exceeds(Entry<T> e){return (maxBatches>0&&1>maxBatches)||(maxRecords>0&&e.records>maxRecords)||(maxBytes>0&&e.bytes>maxBytes);}
    private T readInternal() throws Exception {lock.lockInterruptibly();try{while(buffer.isEmpty()&&remainingProducers>0&&failure==null&&!cancelled){long s=System.nanoTime();try{notEmpty.await();}finally{metrics.addReadBlockedNanos(System.nanoTime()-s);}}if(!buffer.isEmpty()){Entry<T>e=buffer.removeFirst();bufferedRecords-=e.records;bufferedBytes-=e.bytes;metrics.recordDequeued(buffer.size(),bufferedRecords,bufferedBytes);notFull.signalAll();return e.value;}ensureActive();return null;}finally{lock.unlock();}}
    private Entry<T> entry(T value){ if(value instanceof RecordEnvelope){RecordBatch<?> b=((RecordEnvelope<?>)value).getBatch();return new Entry<T>(value,b.size(),b.getEstimatedBytes());} if(value instanceof RecordBatch){RecordBatch<?>b=(RecordBatch<?>)value;return new Entry<T>(value,b.size(),b.getEstimatedBytes());}return new Entry<T>(value,1,RecordSizeEstimator.estimate(value));}
    private void producerFinished(){lock.lock();try{if(remainingProducers>0)--remainingProducers;if(remainingProducers==0)notEmpty.signalAll();}finally{lock.unlock();}}
    private void signalAll(){notEmpty.signalAll();notFull.signalAll();}
    private void ensureActive(){if(failure!=null)throw new IllegalStateException("Channel '"+channelId+"' execution failed",failure);if(cancelled)throw new CancellationException("Channel '"+channelId+"' has been cancelled");}
    private static final class Entry<E>{final E value;final long records,bytes;Entry(E value,long records,long bytes){this.value=value;this.records=records;this.bytes=bytes;}}
    private final class LocalChannelWriter implements ChannelWriter<T>{private final AtomicBoolean closed=new AtomicBoolean();public void write(T value)throws Exception{if(closed.get())throw new IllegalStateException("Channel writer has already been closed");writeInternal(value);}public void fail(Throwable cause){LocalDataChannel.this.fail(cause);}public void close(){if(closed.compareAndSet(false,true))producerFinished();}}
    private final class LocalChannelReader implements ChannelReader<T>{public T read()throws Exception{return readInternal();}}
}
