package com.baize.flux.api.table.type;

import java.io.Serializable;


public interface FluxDataType<T> extends Serializable {
    Class<T> getTypeClass();
    SqlType getSqlType();
}