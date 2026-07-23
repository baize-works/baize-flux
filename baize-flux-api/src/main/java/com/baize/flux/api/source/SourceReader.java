package com.baize.flux.api.source;


import com.baize.flux.api.table.FluxRecord;


public interface SourceReader extends AutoCloseable {


    void open() throws Exception;


    boolean hasNext() throws Exception;


    FluxRecord read() throws Exception;


    @Override
    void close();

}