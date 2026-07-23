package com.baize.flux.framework.channel;
public interface Channel<T> { void put(T value) throws InterruptedException; T take() throws InterruptedException; }
