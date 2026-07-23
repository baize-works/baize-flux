package com.baize.flux.api.table;


import java.io.Serializable;



public class FluxRecord
        implements Serializable {
    private final FluxRow row;
    private final FluxSchema schema;

    public FluxRecord(
            FluxRow row,
            FluxSchema schema){

        this.row=row;
        this.schema=schema;
    }

    public FluxRow getRow(){

        return row;
    }

    public FluxSchema getSchema(){

        return schema;
    }
}