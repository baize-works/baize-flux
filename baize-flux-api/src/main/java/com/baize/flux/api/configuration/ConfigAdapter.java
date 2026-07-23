package com.baize.flux.api.configuration;

import java.nio.file.Path;
import java.util.Map;

/** Adapter mode to support convert other config to HOCON. */
public interface ConfigAdapter {

    /**
     * Provides the config file extension identifier supported by the adapter.
     *
     * @return Extension identifier.
     */
    String[] extensionIdentifiers();

    /**
     * Converter config file to path_key-value Map in HOCON
     *
     * @param configFilePath config file path.
     * @return Map
     */
    Map<String, Object> loadConfig(Path configFilePath);
}
