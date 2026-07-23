package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.configuration.ConfigValidator;
import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.source.BoundedSource;
import com.baize.flux.api.source.SourceReader;
import com.baize.flux.api.source.SourceSplit;
import com.baize.flux.api.table.FluxRow;
import com.baize.flux.api.table.RecordBatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JdbcSourceFactoryTest {
    private static final String URL = "jdbc:h2:mem:jdbc_source;DB_CLOSE_DELAY=-1";

    @Before
    public void setUp() throws Exception {
        Class.forName("org.h2.Driver");
        try (Connection connection = DriverManager.getConnection(URL); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS source_rows");
            statement.execute("CREATE TABLE source_rows (id INT PRIMARY KEY, name VARCHAR(32))");
            statement.execute("INSERT INTO source_rows VALUES (1, 'one'), (2, 'two'), (3, 'three')");
        }
    }

    @After
    public void tearDown() throws Exception {
        try (Connection connection = DriverManager.getConnection(URL); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS source_rows");
        }
    }

    @Test
    public void readsFiniteQueryInConfiguredBatches() throws Exception {
        JdbcSourceFactory factory = new JdbcSourceFactory();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("url", URL);
        values.put("query", "SELECT id, name FROM source_rows ORDER BY id");
        values.put("fetch-size", 2);
        ReadonlyConfig config = ReadonlyConfig.fromMap(values);
        ConfigValidator.strict().validate(config, factory.optionRule()).throwIfInvalid();

        BoundedSource<FluxRow, ?> source = factory.createSource(config);
        List<?> splits = source.planSplits(1);
        assertEquals(1, splits.size());
        SourceReader reader = source.createReader();
        try {
            reader.open((SourceSplit) splits.get(0));
            RecordBatch<FluxRow> first = (RecordBatch<FluxRow>) reader.pollBatch();
            assertEquals(2, first.records().size());
            assertEquals("ID", first.records().get(0).rowType().fieldNames().get(0));
            assertEquals(1, first.records().get(0).getField(0));
            assertEquals("two", first.records().get(1).getField(1));

            RecordBatch<FluxRow> second = (RecordBatch<FluxRow>) reader.pollBatch();
            assertEquals(1, second.records().size());
            assertEquals(3, second.records().get(0).getField(0));
            assertFalse(reader.isFinished());

            RecordBatch<FluxRow> end = (RecordBatch<FluxRow>) reader.pollBatch();
            assertTrue(end.isEndOfInput());
            assertTrue(reader.isFinished());
        } finally {
            reader.close();
        }
    }
}
