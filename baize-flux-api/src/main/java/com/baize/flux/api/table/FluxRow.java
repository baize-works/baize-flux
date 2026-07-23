package com.baize.flux.api.table;


import java.io.Serializable;

public class FluxRow implements Serializable {

    private final Object[] fields;
    public FluxRow(
            Object[] fields) {

        this.fields = fields;
    }

    public Object getField(
            int index) {
        return fields[index];
    }

    public Object[] getFields() {
        return fields;
    }

    public int size() {
        return fields.length;
    }
}