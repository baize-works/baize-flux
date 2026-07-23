package com.baize.flux.api.configuration;

import java.util.Map;

/**
 * The interface that provides the ability to encrypt and decrypt {@link
 */
public interface ConfigShade {

    /**
     * The unique identifier of the current interface, used it to select the correct {@link
     * ConfigShade}
     */
    String getIdentifier();

    /**
     * Encrypt the content
     *
     * @param content The content to encrypt
     */
    String encrypt(String content);

    /**
     * Decrypt the content
     *
     * @param content The content to decrypt
     */
    String decrypt(String content);

    /** To expand the options that user want to encrypt */
    default String[] sensitiveOptions() {
        return new String[0];
    }

    /**
     * this method will be called before the encrypt/decrpyt method. Users can use the props to
     * control the behavior of the encrypt/decrypt
     *
     * @param props the additional properties defined with the key `shade.props` in the
     *     configuration
     */
    default void open(Map<String, Object> props) {
        // default do nothing
    }
}
