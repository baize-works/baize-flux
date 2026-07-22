package com.baize.flux.framework.configuration;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigResolveOptions;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

/** Loads HOCON into the engine-neutral {@link ReadonlyConfig}. */
public final class HoconConfigLoader {

    public ReadonlyConfig load(Path path) {
        Objects.requireNonNull(path, "path");
        String source = path.toAbsolutePath().toString();
        try {
            Config config =
                    ConfigFactory.parseFile(
                                    path.toFile(),
                                    ConfigParseOptions.defaults().setAllowMissing(false))
                            .resolve(ConfigResolveOptions.defaults());
            return fromConfig(config);
        } catch (ConfigException e) {
            throw parseException(source, e);
        }
    }

    public ReadonlyConfig parse(String content) {
        Objects.requireNonNull(content, "content");
        try {
            return fromConfig(ConfigFactory.parseString(content).resolve());
        } catch (ConfigException e) {
            throw parseException("<string>", e);
        }
    }

    private ReadonlyConfig fromConfig(Config config) {
        Map<String, Object> values = config.root().unwrapped();
        return ReadonlyConfig.fromMap(values);
    }

    private ConfigParseException parseException(String source, ConfigException exception) {
        Integer line =
                exception.origin() == null || exception.origin().lineNumber() <= 0
                        ? null
                        : exception.origin().lineNumber();
        String location = line == null ? source : source + ":" + line;
        return new ConfigParseException(
                source,
                line,
                "Failed to parse HOCON configuration at "
                        + location
                        + ": "
                        + exception.getMessage(),
                exception);
    }
}
