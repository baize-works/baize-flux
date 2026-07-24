package com.baize.flux.framework.plugin;

import java.net.URL;
import java.net.URLClassLoader;

/** Isolates connector implementation dependencies while sharing Flux API classes. */
public final class ConnectorClassLoader extends URLClassLoader {
    static { registerAsParallelCapable(); }
    public ConnectorClassLoader(URL[] urls, ClassLoader parent) { super(urls, parent); }
    @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                if (parentFirst(name)) loaded = getParent().loadClass(name);
                else try { loaded = findClass(name); } catch (ClassNotFoundException absent) { loaded = getParent().loadClass(name); }
            }
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }
    private static boolean parentFirst(String name) {
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.")
                || name.startsWith("sun.") || name.startsWith("com.baize.flux.api.")
                || name.startsWith("org.slf4j.") || name.startsWith("org.apache.logging.")
                || name.startsWith("org.apache.log4j.");
    }
}
