package com.baize.flux.framework.execution.split;

import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.framework.execution.CancellationToken;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LocalSplitQueueTest {

    @Test
    public void acquiresEachSplitOnlyOnceAcrossWorkers() throws Exception {
        final LocalSplitQueue<TestSplit> queue = new LocalSplitQueue<TestSplit>(Arrays.asList(
                new TestSplit("a"), new TestSplit("b"), new TestSplit("c"), new TestSplit("d")));
        final Set<String> acquired = Collections.synchronizedSet(new HashSet<String>());
        Runnable worker = new Runnable() {
            @Override public void run() {
                try {
                    TestSplit split;
                    while ((split = queue.acquire(new CancellationToken())) != null) {
                        assertTrue(acquired.add(split.splitId()));
                        queue.complete(split);
                    }
                } catch (Exception e) { throw new AssertionError(e); }
            }
        };
        Thread first = new Thread(worker);
        Thread second = new Thread(worker);
        first.start(); second.start(); first.join(); second.join();
        assertEquals(4, acquired.size());
        assertEquals(4, queue.getCompletedSplitCount());
    }

    @Test
    public void cancellationWakesBlockedAcquire() throws Exception {
        final LocalSplitQueue<TestSplit> queue = new LocalSplitQueue<TestSplit>(Arrays.asList(new TestSplit("a")));
        final CancellationToken token = new CancellationToken();
        final TestSplit split = queue.acquire(token);
        final CountDownLatch stopped = new CountDownLatch(1);
        Thread waiter = new Thread(new Runnable() {
            @Override public void run() {
                try { queue.acquire(token); } catch (RuntimeException expected) { stopped.countDown(); }
                catch (InterruptedException e) { throw new AssertionError(e); }
            }
        });
        waiter.start();
        Thread.sleep(50L);
        token.cancel(new RuntimeException("cancelled"));
        assertTrue(stopped.await(2L, TimeUnit.SECONDS));
        queue.returnSplit(split);
    }

    private static final class TestSplit implements SourceSplit {
        private final String id;
        private TestSplit(String id) { this.id = id; }
        @Override public String splitId() { return id; }
    }
}
