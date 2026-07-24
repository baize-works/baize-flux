package com.baize.flux.api.table.catalog.exception;

import com.baize.flux.common.exception.FluxRuntimeException;
import com.baize.flux.common.exception.error.FluxAPIErrorCode;

/**
 * Catalog 操作异常。
 */
public class CatalogException extends FluxRuntimeException {

    private static final long serialVersionUID = 1L;

    public CatalogException(String message) {
        super(FluxAPIErrorCode.CATALOG_INITIALIZE_FAILED, message);
    }

    public CatalogException(
            String message,
            Throwable cause) {

        super(FluxAPIErrorCode.CATALOG_INITIALIZE_FAILED, message, cause);
    }

    public CatalogException(Throwable cause) {
        super(FluxAPIErrorCode.CATALOG_INITIALIZE_FAILED, cause);
    }
}
