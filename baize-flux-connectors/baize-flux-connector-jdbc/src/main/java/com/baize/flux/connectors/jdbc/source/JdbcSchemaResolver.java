package com.baize.flux.connectors.jdbc.source;

import com.baize.flux.api.table.FluxSchema;
import com.baize.flux.api.table.type.*;


import java.sql.*;


public class JdbcSchemaResolver {


    public static FluxSchema resolve(
            ResultSetMetaData meta)
            throws Exception {


        int size =
                meta.getColumnCount();


        String[] names =
                new String[size];


        FluxDataType<?>[] types =
                new FluxDataType[size];



        for(int i=0;i<size;i++){


            names[i]
                    =
                    meta.getColumnName(i+1);


            types[i]
                    =
                    convert(
                            meta.getColumnType(i+1)
                    );

        }


        return new FluxSchema(
                names,
                types
        );

    }



    private static FluxDataType<?> convert(int jdbcType) {

        switch (jdbcType) {

            case Types.BIGINT:
                return BasicType.LONG;

            case Types.INTEGER:
                return BasicType.INT;

            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
                return BasicType.STRING;

            default:
                return BasicType.STRING;
        }
    }

}
