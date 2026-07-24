package com.baize.flux.api.factory;

import com.baize.flux.api.connector.ConnectorDescriptor;

import java.util.Collections;

/**
 * Flux 插件工厂基础接口。
 * <p>
 * Source、Sink 等插件工厂都需要实现该接口。
 */
public interface Factory {

    /**
     * 工厂唯一标识。
     * <p>
     * 例如：jdbc、mysql-cdc、file。
     */
    String factoryIdentifier();

    /** Metadata for diagnostics and plugin discovery. */
    default ConnectorDescriptor connectorDescriptor() {
        return new ConnectorDescriptor(factoryIdentifier(), "unknown", "1",
                Collections.<ConnectorDescriptor.Type>emptySet(),
                Collections.<String>emptySet(), null);
    }
}