package com.baize.flux.framework.connector;

import com.baize.flux.common.exception.FluxRuntimeException;
import com.baize.flux.common.exception.error.FluxAPIErrorCode;

/**
 * Connector 发现、校验或准备失败。
 */
public class ConnectorException
        extends FluxRuntimeException {

    private static final long serialVersionUID = 1L;

    public ConnectorException(String message) {
        super(FluxAPIErrorCode.CONNECTOR_INITIALIZE_FAILED, message);
    }

    public ConnectorException(
            String message,
            Throwable cause) {

        super(FluxAPIErrorCode.CONNECTOR_INITIALIZE_FAILED, message, cause);
    }
}
