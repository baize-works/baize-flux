package com.alibaba.datax.plugin.reader.oceanbasev10reader.util;

import com.alibaba.datax.common.constant.CommonConstant;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.plugin.rdbms.reader.Constant;
import com.alibaba.datax.plugin.rdbms.reader.Key;
import com.alibaba.datax.plugin.rdbms.reader.util.HintUtil;
import com.alibaba.datax.plugin.rdbms.reader.util.SingleTableSplitUtil;
import com.alibaba.datax.plugin.rdbms.util.DataBaseType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * OceanBase Reader 专用的 Job 切分逻辑，在 splitPk 场景下使用 {@link ObSingleTableSplitUtil}。
 */
public final class ObReaderSplitUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ObReaderSplitUtil.class);

    private ObReaderSplitUtil() {
    }

    public static List<Configuration> doSplit(Configuration originalSliceConfig, int adviceNumber) {
        boolean isTableMode = originalSliceConfig.getBool(Constant.IS_TABLE_MODE).booleanValue();
        int eachTableShouldSplittedNumber = -1;
        if (isTableMode) {
            eachTableShouldSplittedNumber = calculateEachTableShouldSplittedNumber(
                    adviceNumber, originalSliceConfig.getInt(Constant.TABLE_NUMBER_MARK));
        }

        String column = originalSliceConfig.getString(Key.COLUMN);
        String where = originalSliceConfig.getString(Key.WHERE, null);
        List<Object> conns = originalSliceConfig.getList(Constant.CONN_MARK, Object.class);
        List<Configuration> splittedConfigs = new ArrayList<Configuration>();

        for (int i = 0, len = conns.size(); i < len; i++) {
            Configuration sliceConfig = originalSliceConfig.clone();
            Configuration connConf = Configuration.from(conns.get(i).toString());
            String jdbcUrl = connConf.getString(Key.JDBC_URL);
            sliceConfig.set(Key.JDBC_URL, jdbcUrl);
            sliceConfig.set(CommonConstant.LOAD_BALANCE_RESOURCE_MARK, DataBaseType.parseIpFromJdbcUrl(jdbcUrl));
            sliceConfig.remove(Constant.CONN_MARK);

            if (isTableMode) {
                List<String> tables = connConf.getList(Key.TABLE, String.class);
                Validate.isTrue(null != tables && !tables.isEmpty(), "您读取数据库表配置错误.");

                String splitPk = originalSliceConfig.getString(Key.SPLIT_PK, null);
                boolean needSplitTable = eachTableShouldSplittedNumber > 1
                        && StringUtils.isNotBlank(splitPk);
                if (needSplitTable) {
                    if (tables.size() == 1) {
                        Integer splitFactor = originalSliceConfig.getInt(Key.SPLIT_FACTOR, Constant.SPLIT_FACTOR);
                        eachTableShouldSplittedNumber = eachTableShouldSplittedNumber * splitFactor;
                    }
                    for (String table : tables) {
                        Configuration tempSlice = sliceConfig.clone();
                        tempSlice.set(Key.TABLE, table);
                        splittedConfigs.addAll(
                                ObSingleTableSplitUtil.splitSingleTable(tempSlice, eachTableShouldSplittedNumber));
                    }
                } else {
                    for (String table : tables) {
                        Configuration tempSlice = sliceConfig.clone();
                        tempSlice.set(Key.TABLE, table);
                        String queryColumn = HintUtil.buildQueryColumn(jdbcUrl, table, column);
                        tempSlice.set(Key.QUERY_SQL,
                                SingleTableSplitUtil.buildQuerySql(queryColumn, table, where));
                        splittedConfigs.add(tempSlice);
                    }
                }
            } else {
                List<String> sqls = connConf.getList(Key.QUERY_SQL, String.class);
                for (String querySql : sqls) {
                    Configuration tempSlice = sliceConfig.clone();
                    tempSlice.set(Key.QUERY_SQL, querySql);
                    splittedConfigs.add(tempSlice);
                }
            }
        }

        return splittedConfigs;
    }

    private static int calculateEachTableShouldSplittedNumber(int adviceNumber, int tableNumber) {
        return (int) Math.ceil(1.0 * adviceNumber / tableNumber);
    }
}
