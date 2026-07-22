package com.baize.flux.api.configuration;

/**
 * Converts a raw configuration value to the type expected by an {@link Option}.
 */
@FunctionalInterface
public interface ConfigConverter<T> {

    T convert(Object rawValue);
}
