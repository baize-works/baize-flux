package com.baize.flux.connectors.jdbc;

import com.baize.flux.api.configuration.ReadonlyConfig;
import com.baize.flux.api.job.Boundedness;
import com.baize.flux.api.job.JobDefinition;
import com.baize.flux.framework.execution.JobResult;
import com.baize.flux.framework.execution.LocalJobExecutor;
import com.baize.flux.framework.planner.JobPlanner;
import com.baize.flux.framework.plugin.FactoryRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Verifies ServiceLoader discovery and the bounded JDBC-to-JDBC execution path.
 */
public class JdbcEndToEndTest {
    private static ReadonlyConfig config(String url, String key, Object value, String optionalKey, Object optionalValue) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("url", url);
        values.put(key, value);
        values.put(optionalKey, optionalValue);
        return ReadonlyConfig.fromMap(values);
    }

    @Test
    public void copiesRowsAndCommitsTransaction() throws Exception {
        String sourceUrl = "jdbc:h2:mem:source;DB_CLOSE_DELAY=-1";
        String sinkUrl = "jdbc:h2:mem:sink;DB_CLOSE_DELAY=-1";
        try (Connection source = DriverManager.getConnection(sourceUrl); Statement statement = source.createStatement()) {
            statement.execute("CREATE TABLE input (id INT, name VARCHAR(32))");
            statement.execute("INSERT INTO input VALUES (1, 'one'), (2, 'two')");
        }
        try (Connection sink = DriverManager.getConnection(sinkUrl); Statement statement = sink.createStatement()) {
            statement.execute("CREATE TABLE output (id INT, name VARCHAR(32))");
        }
        JobDefinition job = new JobDefinition("copy", Boundedness.BOUNDED, 2, "jdbc", config(sourceUrl, "query", "SELECT id, name FROM input", "fetch-size", 2), "jdbc", config(sinkUrl, "table", "output", "batch-size", 2));
        JobResult result = new LocalJobExecutor().execute(new JobPlanner(FactoryRegistry.discover()).plan(job));
        assertEquals(2, result.readRecords());
        assertEquals(2, result.writtenRecords());
        try (Connection sink = DriverManager.getConnection(sinkUrl); Statement statement = sink.createStatement(); ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM output")) {
            rows.next();
            assertEquals(2, rows.getInt(1));
        }
    }
}
