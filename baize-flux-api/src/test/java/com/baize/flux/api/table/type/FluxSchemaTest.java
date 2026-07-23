package com.baize.flux.api.table.type;

import com.baize.flux.api.table.FluxRecord;
import com.baize.flux.api.table.FluxRow;
import com.baize.flux.api.table.FluxSchema;
import org.junit.Assert;
import org.junit.Test;

public class FluxSchemaTest {

    @Test
    public void testCreateSchemaAndRecord() {

        FluxSchema schema = new FluxSchema(
                new String[]{"id", "name", "age"},
                new FluxDataType[]{
                        BasicType.LONG,
                        BasicType.STRING,
                        BasicType.INT
                }
        );

        Assert.assertEquals(3, schema.size());
        Assert.assertEquals("id", schema.getFieldName(0));
        Assert.assertEquals("name", schema.getFieldName(1));
        Assert.assertEquals(BasicType.LONG, schema.getFieldType(0));

        FluxRow row = new FluxRow(
                new Object[]{1001L, "zhangsan", 18}
        );

        Assert.assertEquals(1001L, row.getField(0));
        Assert.assertEquals("zhangsan", row.getField(1));
        Assert.assertEquals(18, row.getField(2));

        FluxRecord record = new FluxRecord(row, schema);

        Assert.assertNotNull(record.getSchema());
        Assert.assertNotNull(record.getRow());
        Assert.assertEquals(schema, record.getSchema());
    }
}