package com.baize.flux.framework.factory;

/**
 * Factory 发现、校验或创建失败。
 */
public class FactoryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FactoryException(String message) {
        super(message);
    }

    public FactoryException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}