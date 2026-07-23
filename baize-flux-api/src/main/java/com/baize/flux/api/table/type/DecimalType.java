package com.baize.flux.api.table.type;
import java.math.BigDecimal;

public final class DecimalType
        extends BasicType<BigDecimal> {
    private final int precision;
    private final int scale;
    public DecimalType(
            int precision,
            int scale){
        super(
                BigDecimal.class,
                SqlType.DECIMAL);
        this.precision=precision;
        this.scale=scale;
    }
    public int getPrecision(){

        return precision;
    }
    public int getScale(){
        return scale;
    }

}