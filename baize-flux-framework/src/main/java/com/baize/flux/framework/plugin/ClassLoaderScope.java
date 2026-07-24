package com.baize.flux.framework.plugin;

import java.util.Objects;
/** Temporarily installs a connector loader as the thread context class loader. */
public final class ClassLoaderScope implements AutoCloseable {
    private final Thread thread; private final ClassLoader previous;
    private ClassLoaderScope(ClassLoader loader) { thread = Thread.currentThread(); previous = thread.getContextClassLoader(); thread.setContextClassLoader(Objects.requireNonNull(loader, "loader")); }
    public static ClassLoaderScope open(ClassLoader loader) { return new ClassLoaderScope(loader); }
    @Override public void close() { thread.setContextClassLoader(previous); }
}
