package com.baize.flux.connectors.jdbc.source;

import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.table.*;


import java.sql.*;


public class JdbcSourceReader
        implements SourceReader {


    private final JdbcSourceConfig config;


    private Connection connection;

    private PreparedStatement statement;

    private ResultSet resultSet;


    private FluxSchema schema;



    public JdbcSourceReader(
            JdbcSourceConfig config){

        this.config=config;
    }



    @Override
    public void open()
            throws Exception {


        Class.forName(
                config.getDriver()
        );


        connection =
                DriverManager.getConnection(
                        config.getUrl(),
                        config.getUsername(),
                        config.getPassword()
                );


        statement =
                connection.prepareStatement(
                        config.getSql()
                );


        resultSet =
                statement.executeQuery();


        schema =
                JdbcSchemaResolver.resolve(
                        resultSet.getMetaData()
                );

    }



    @Override
    public boolean hasNext()
            throws Exception {


        return resultSet.next();

    }



    @Override
    public FluxRecord read()
            throws Exception {


        int count =
                resultSet.getMetaData()
                        .getColumnCount();


        Object[] values =
                new Object[count];


        for(int i=0;i<count;i++){

            values[i]
                    =
                    resultSet.getObject(i+1);

        }


        FluxRow row =
                new FluxRow(values);



        return new FluxRecord(
                row,
                schema
        );

    }



    @Override
    public void close(){


        try {

            if(resultSet!=null)
                resultSet.close();


            if(statement!=null)
                statement.close();


            if(connection!=null)
                connection.close();


        }catch(Exception ignored){

        }

    }

}