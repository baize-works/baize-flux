package com.baize.flux.api.connector;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Immutable metadata used to identify a connector plugin. */
public final class ConnectorDescriptor {
    public enum Type { SOURCE, SINK }
    private final String identifier;
    private final String version;
    private final String apiVersion;
    private final Set<Type> types;
    private final Set<String> requiredCapabilities;
    private final String pluginPath;

    public ConnectorDescriptor(String identifier, String version, String apiVersion,
            Set<Type> types, Set<String> requiredCapabilities, String pluginPath) {
        this.identifier = require(identifier, "identifier");
        this.version = require(version, "version");
        this.apiVersion = require(apiVersion, "apiVersion");
        this.types = Collections.unmodifiableSet(new LinkedHashSet<Type>(Objects.requireNonNull(types, "types")));
        this.requiredCapabilities = Collections.unmodifiableSet(new LinkedHashSet<String>(Objects.requireNonNull(requiredCapabilities, "requiredCapabilities")));
        this.pluginPath = pluginPath;
    }
    private static String require(String value, String name) { if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(name + " must not be blank"); return value; }
    public String getIdentifier() { return identifier; }
    public String getVersion() { return version; }
    public String getApiVersion() { return apiVersion; }
    public Set<Type> getTypes() { return types; }
    public Set<String> getRequiredCapabilities() { return requiredCapabilities; }
    public String getPluginPath() { return pluginPath; }
    public ConnectorDescriptor withPluginPath(String path) { return new ConnectorDescriptor(identifier, version, apiVersion, types, requiredCapabilities, path); }
}
