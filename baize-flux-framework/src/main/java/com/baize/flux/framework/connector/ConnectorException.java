package com.baize.flux.framework.connector;

/**
 * Connector 发现、校验或准备失败。
 */
public class ConnectorException
        extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectorException(String message) {
        super(message);
    }

    public ConnectorException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}