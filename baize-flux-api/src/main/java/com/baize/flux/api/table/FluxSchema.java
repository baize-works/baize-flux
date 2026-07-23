package com.baize.flux.api.table;


import com.baize.flux.api.table.type.FluxDataType;


public class FluxSchema {
    private final String[] fieldNames;
    private final FluxDataType<?>[] fieldTypes;
    public FluxSchema(
            String[] fieldNames,
            FluxDataType<?>[] fieldTypes){

        if(fieldNames.length != fieldTypes.length){
            throw new IllegalArgumentException(
                    "field size mismatch");
        }
        this.fieldNames=fieldNames;
        this.fieldTypes=fieldTypes;
    }
    public int size(){
        return fieldNames.length;
    }
    public String getFieldName(int index){

        return fieldNames[index];
    }

    public FluxDataType<?> getFieldType(int index){
        return fieldTypes[index];
    }
}