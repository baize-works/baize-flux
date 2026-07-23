package com.baize.flux.api.table.type;
import java.util.Objects;

public class BasicType<T>
        implements FluxDataType<T> {
    public static final BasicType<String>
            STRING =
            new BasicType<>(String.class, SqlType.STRING);
    public static final BasicType<Integer>
            INT =
            new BasicType<>(Integer.class, SqlType.INT);
    public static final BasicType<Long>
            LONG =
            new BasicType<>(Long.class, SqlType.BIGINT);
    public static final BasicType<Boolean>
            BOOLEAN =
            new BasicType<>(Boolean.class, SqlType.BOOLEAN);
    private final Class<T> typeClass;
    private final SqlType sqlType;
    public BasicType(
            Class<T> typeClass,
            SqlType sqlType) {

        this.typeClass = typeClass;
        this.sqlType = sqlType;
    }
    @Override
    public Class<T> getTypeClass() {
        return typeClass;
    }
    @Override
    public SqlType getSqlType() {
        return sqlType;
    }
    @Override
    public boolean equals(Object o){
        if(this==o){
            return true;
        }
        if(!(o instanceof BasicType)){
            return false;
        }
        BasicType<?> that=(BasicType<?>)o;
        return Objects.equals(
                typeClass,
                that.typeClass)
                &&
                sqlType==that.sqlType;
    }
    @Override
    public int hashCode(){
        return Objects.hash(
                typeClass,
                sqlType);
    }
    @Override
    public String toString(){
        return sqlType.name();
    }

}