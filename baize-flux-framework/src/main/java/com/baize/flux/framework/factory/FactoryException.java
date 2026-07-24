package com.baize.flux.framework.factory;

import com.baize.flux.api.exception.FluxRuntimeException;
import com.baize.flux.api.exception.error.FluxApiErrorCode;

/**
 * Factory 发现、校验或创建失败。
 */
public class FactoryException extends FluxRuntimeException {

    private static final long serialVersionUID = 1L;

    public FactoryException(String message) {
        super(FluxApiErrorCode.FACTORY_INITIALIZE_FAILED, message);
    }

    public FactoryException(
            String message,
            Throwable cause) {
        super(FluxApiErrorCode.FACTORY_INITIALIZE_FAILED, message, cause);
    }
}
