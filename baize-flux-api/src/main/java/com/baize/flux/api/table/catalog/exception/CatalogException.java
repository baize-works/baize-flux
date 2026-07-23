package com.baize.flux.api.table.catalog.exception;

/**
 * Catalog 操作异常。
 */
public class CatalogException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CatalogException(String message) {
        super(message);
    }

    public CatalogException(
            String message,
            Throwable cause) {

        super(message, cause);
    }

    public CatalogException(Throwable cause) {
        super(cause);
    }
}
