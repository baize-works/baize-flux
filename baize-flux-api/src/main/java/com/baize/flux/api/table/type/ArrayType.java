package com.baize.flux.api.table.type;

import java.lang.reflect.Array;
public class ArrayType<T>
        implements FluxDataType<T>{
    private final Class<T> typeClass;
    private final FluxDataType<?> elementType;
    public ArrayType(
            Class<T> typeClass,
            FluxDataType<?> elementType){
        this.typeClass=typeClass;
        this.elementType=elementType;
    }
    public static ArrayType<?> of(
            FluxDataType<?> type){
        Class<?> clazz =
                Array.newInstance(
                        type.getTypeClass(),
                        0)
                        .getClass();
        return new ArrayType<>(
                clazz,
                type);
    }
    public FluxDataType<?> getElementType(){

        return elementType;
    }
    @Override
    public Class<T> getTypeClass(){

        return typeClass;
    }
    @Override
    public SqlType getSqlType(){
        return SqlType.ARRAY;
    }
}