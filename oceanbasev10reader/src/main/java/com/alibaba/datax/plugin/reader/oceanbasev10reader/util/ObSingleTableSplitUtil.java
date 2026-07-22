package com.alibaba.datax.plugin.reader.oceanbasev10reader.util;

import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.reader.Constant;
import com.alibaba.datax.plugin.rdbms.reader.Key;
import com.alibaba.datax.plugin.rdbms.reader.util.SingleTableSplitUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtil;
import com.alibaba.datax.plugin.rdbms.util.DBUtilErrorCode;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import com.alibaba.datax.plugin.rdbms.util.RdbmsException;
import com.alibaba.datax.plugin.rdbms.util.RdbmsRangeSplitWrap;
import com.alibaba.datax.plugin.reader.oceanbasev10reader.ext.ObReaderKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * OceanBase Reader 专用的单表切分逻辑，包含字符串 splitPk（Oracle 模式）及 OB V10 min/max 查询优化。
 */
public final class ObSingleTableSplitUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ObSingleTableSplitUtil.class);

    private static final DataBaseType DATABASE_TYPE = ObReaderUtils.databaseType;

    private ObSingleTableSplitUtil() {
    }

    public static List<Configuration> splitSingleTable(Configuration configuration, int adviceNum) {
        List<Configuration> pluginParams = new ArrayList<Configuration>();
        List<String> rangeList;
        String splitPkName = configuration.getString(Key.SPLIT_PK);
        String column = configuration.getString(Key.COLUMN);
        String table = configuration.getString(Key.TABLE);
        String where = configuration.getString(Key.WHERE, null);
        boolean hasWhere = StringUtils.isNotBlank(where);

        Pair<Object, Object> minMaxPK = getPkRange(configuration);
        if (null == minMaxPK) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "根据切分主键切分表失败. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
        }

        configuration.set(Key.QUERY_SQL, SingleTableSplitUtil.buildQuerySql(column, table, where));
        if (null == minMaxPK.getLeft() || null == minMaxPK.getRight()) {
            pluginParams.add(configuration);
            return pluginParams;
        }

        boolean isStringType = Constant.PK_TYPE_STRING.equals(configuration.getString(Constant.PK_TYPE));
        boolean isLongType = Constant.PK_TYPE_LONG.equals(configuration.getString(Constant.PK_TYPE));

        if (isStringType) {
            DataBaseType stringSplitDbType = resolveStringSplitDataBaseType(configuration);
            if (stringSplitDbType == null) {
                pluginParams.add(configuration);
                LOG.warn("切分主键 {} 为字符串类型，当前 OceanBase MySQL 模式不支持按字符串切分，将使用单通道读取。",
                        splitPkName);
                return pluginParams;
            }
            rangeList = RdbmsRangeSplitWrap.splitAndWrap(
                    String.valueOf(minMaxPK.getLeft()),
                    String.valueOf(minMaxPK.getRight()), adviceNum,
                    splitPkName, "'", stringSplitDbType);
        } else if (isLongType) {
            rangeList = RdbmsRangeSplitWrap.splitAndWrap(
                    new BigInteger(minMaxPK.getLeft().toString()),
                    new BigInteger(minMaxPK.getRight().toString()),
                    adviceNum, splitPkName);
        } else {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "您配置的切分主键(splitPk) 类型 DataX 不支持. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
        }

        String tempQuerySql;
        List<String> allQuerySql = new ArrayList<String>();

        if (null != rangeList && !rangeList.isEmpty()) {
            for (String range : rangeList) {
                Configuration tempConfig = configuration.clone();
                tempQuerySql = SingleTableSplitUtil.buildQuerySql(column, table, where)
                        + (hasWhere ? " and " : " where ") + range;
                allQuerySql.add(tempQuerySql);
                tempConfig.set(Key.QUERY_SQL, tempQuerySql);
                tempConfig.set(Key.WHERE, (hasWhere ? ("(" + where + ") and") : "") + range);
                pluginParams.add(tempConfig);
            }
        } else {
            Configuration tempConfig = configuration.clone();
            tempQuerySql = SingleTableSplitUtil.buildQuerySql(column, table, where)
                    + (hasWhere ? " and " : " where ")
                    + String.format(" %s IS NOT NULL", splitPkName);
            allQuerySql.add(tempQuerySql);
            tempConfig.set(Key.QUERY_SQL, tempQuerySql);
            tempConfig.set(Key.WHERE, (hasWhere ? "(" + where + ") and" : "")
                    + String.format(" %s IS NOT NULL", splitPkName));
            pluginParams.add(tempConfig);
        }

        Configuration tempConfig = configuration.clone();
        tempQuerySql = SingleTableSplitUtil.buildQuerySql(column, table, where)
                + (hasWhere ? " and " : " where ")
                + String.format(" %s IS NULL", splitPkName);
        allQuerySql.add(tempQuerySql);
        LOG.info("After split(), allQuerySql=[\n{}\n].", StringUtils.join(allQuerySql, "\n"));
        tempConfig.set(Key.QUERY_SQL, tempQuerySql);
        tempConfig.set(Key.WHERE, (hasWhere ? "(" + where + ") and" : "")
                + String.format(" %s IS NULL", splitPkName));
        pluginParams.add(tempConfig);

        return pluginParams;
    }

    private static Pair<Object, Object> getPkRange(Configuration configuration) {
        int fetchSize = configuration.getInt(Constant.FETCH_SIZE);
        String jdbcURL = configuration.getString(Key.JDBC_URL);
        String username = configuration.getString(Key.USERNAME);
        String password = configuration.getString(Key.PASSWORD);
        String table = configuration.getString(Key.TABLE);

        Connection conn = DBUtil.getConnection(DATABASE_TYPE, jdbcURL, username, password);
        Pair<Object, Object> minMaxPK = checkSplitPk(conn, fetchSize, table, username, configuration);
        DBUtil.closeDBResources(null, null, conn);
        return minMaxPK;
    }

    private static Pair<Object, Object> checkSplitPk(Connection conn, int fetchSize, String table,
                                                     String username, Configuration configuration) {
        LOG.info("Get min/max of split key for OBV10");
        int queryTimeoutSeconds = 60 * 60 * 48;
        String setQueryTimeout = "set ob_query_timeout=" + (queryTimeoutSeconds * 1000 * 1000L);
        String setTrxTimeout = "set ob_trx_timeout=" + ((queryTimeoutSeconds + 5) * 1000 * 1000L);
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(setQueryTimeout);
            stmt.execute(setTrxTimeout);
        } catch (Exception e) {
            LOG.warn("set ob_query_timeout and set ob_trx_timeout failed. reason: {}", e.getMessage(), e);
        } finally {
            DBUtil.closeDBResources(stmt, null);
        }
        return new ImmutablePair<Object, Object>(
                getValueForObV10(conn, "min", configuration),
                getValueForObV10(conn, "max", configuration));
    }

    private static Object getValueForObV10(Connection conn, String function, Configuration configuration) {
        ResultSet rs = null;
        Object value = null;
        final int fetchSize = 1;
        String username = configuration.getString(Key.USERNAME);
        String table = configuration.getString(Key.TABLE);
        String sql = genSqlForObV10(configuration, function);

        LOG.info("Running Query [{}]", sql);

        try {
            rs = DBUtil.query(conn, sql, fetchSize);
            ResultSetMetaData rsMetaData = rs.getMetaData();
            if (isPKTypeValid(rsMetaData)) {
                if (isStringType(rsMetaData.getColumnType(1))) {
                    configuration.set(Constant.PK_TYPE, Constant.PK_TYPE_STRING);
                } else if (isLongType(rsMetaData.getColumnType(1))) {
                    configuration.set(Constant.PK_TYPE, Constant.PK_TYPE_LONG);
                } else {
                    throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                            "您配置的DataX切分主键(splitPk)有误. 因为您配置的切分主键(splitPk) 类型 DataX 不支持. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
                }
                while (DBUtil.asyncResultSetNext(rs)) {
                    value = rs.getString(1);
                }
            } else {
                throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                        "您配置的DataX切分主键(splitPk)有误. 因为您配置的切分主键(splitPk) 类型 DataX 不支持. DataX 仅支持切分主键为一个,并且类型为整数或者字符串类型. 请尝试使用其他的切分主键或者联系 DBA 进行处理.");
            }
        } catch (DataXException e) {
            throw e;
        } catch (Exception e) {
            throw RdbmsException.asQueryException(DATABASE_TYPE, e, sql, table, username);
        } finally {
            DBUtil.closeDBResources(rs, null, null);
        }
        return value;
    }

    private static String genSqlForObV10(Configuration configuration, String function) {
        String primaryKey = configuration.getString(Key.SPLIT_PK).trim();
        String table = configuration.getString(Key.TABLE).trim();
        String where = configuration.getString(Key.WHERE, null);
        String pkRangeSQL = String.format("SELECT %s(%s) FROM %s", function, primaryKey, table);
        if (StringUtils.isNotBlank(where)) {
            pkRangeSQL = String.format("%s WHERE (%s AND %s IS NOT NULL)", pkRangeSQL, where, primaryKey);
        }
        return pkRangeSQL;
    }

    private static DataBaseType resolveStringSplitDataBaseType(Configuration configuration) {
        if (isObOracleMode(configuration)) {
            return DataBaseType.Oracle;
        }
        return null;
    }

    private static boolean isObOracleMode(Configuration configuration) {
        return configuration != null
                && configuration.getString(ObReaderKey.OB_COMPATIBILITY_MODE, "")
                .equalsIgnoreCase(DataBaseType.Oracle.getTypeName());
    }

    private static boolean isPKTypeValid(ResultSetMetaData rsMetaData) {
        try {
            int pkType = rsMetaData.getColumnType(1);
            int maxType = Types.NULL;
            if (rsMetaData.getColumnCount() == 2) {
                maxType = rsMetaData.getColumnType(2);
            }
            boolean isNumberType = isLongType(pkType);
            boolean isStringType = isStringType(pkType);
            return (maxType == Types.NULL || pkType == maxType) && (isNumberType || isStringType);
        } catch (Exception e) {
            throw DataXException.asDataXException(DBUtilErrorCode.ILLEGAL_SPLIT_PK,
                    "DataX获取切分主键(splitPk)字段类型失败. 该错误通常是系统底层异常导致. 请联DBA进行处理.");
        }
    }

    private static boolean isLongType(int type) {
        boolean isValidLongType = type == Types.BIGINT || type == Types.INTEGER
                || type == Types.SMALLINT || type == Types.TINYINT || type == Types.NUMERIC;
        return isValidLongType;
    }

    private static boolean isStringType(int type) {
        return type == Types.CHAR || type == Types.NCHAR
                || type == Types.VARCHAR || type == Types.LONGVARCHAR
                || type == Types.NVARCHAR;
    }
}
