package com.baize.flux.framework.channel;

import java.util.concurrent.ArrayBlockingQueue;

public final class MemoryChannel<T> implements Channel<T> {
    private final ArrayBlockingQueue<T> queue;

    public MemoryChannel(int capacity) {
        queue = new ArrayBlockingQueue<T>(capacity);
    }

    public void put(T value) throws InterruptedException {
        queue.put(value);
    }

    public T take() throws InterruptedException {
        return queue.take();
    }
}
