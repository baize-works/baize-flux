package com.baize.flux.framework.connector;

import com.baize.flux.api.exception.FluxRuntimeException;
import com.baize.flux.api.exception.error.FluxApiErrorCode;

/**
 * Connector 发现、校验或准备失败。
 */
public class ConnectorException
        extends FluxRuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectorException(String message) {
        super(FluxApiErrorCode.CONNECTOR_INITIALIZE_FAILED, message);
    }

    public ConnectorException(
            String message,
            Throwable cause) {

        super(FluxApiErrorCode.CONNECTOR_INITIALIZE_FAILED, message, cause);
    }
}
