/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.datasource;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.sql.SQLUtils;
import easy4j.module.base.utils.SysLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;

/**
 * 数据源日志过滤
 * @author bokun.li
 * @date 2023/11/21
 */
public class Log4j3Filter extends LogFilter implements Log4j3FilterMBean {
    private Logger dataSourceLogger;
    private Logger connectionLogger;
    private Logger statementLogger;
    private Logger resultSetLogger;

    public Log4j3Filter() {
        this.dataSourceLogger = LogManager.getLogger(this.dataSourceLoggerName);
        this.connectionLogger = LogManager.getLogger(this.connectionLoggerName);
        this.statementLogger = LogManager.getLogger(this.statementLoggerName);
        this.resultSetLogger = LogManager.getLogger(this.resultSetLoggerName);
        super.setStatementCloseAfterLogEnabled(false);
        super.setStatementCreateAfterLogEnabled(false);
        super.setStatementParameterClearLogEnable(false);
        super.setStatementSqlFormatOption(new SQLUtils.FormatOption(true,false));
        super.setResultSetOpenAfterLogEnabled(false);
        super.setStatementPrepareAfterLogEnabled(false);
        super.setStatementPrepareCallAfterLogEnabled(false);
        super.setStatementParameterSetLogEnabled(false);
        super.setStatementExecuteAfterLogEnabled(false);
    }

    public String getDataSourceLoggerName() {
        return this.dataSourceLoggerName;
    }

    public void setDataSourceLoggerName(String dataSourceLoggerName) {
        this.dataSourceLoggerName = dataSourceLoggerName;
        this.dataSourceLogger = LogManager.getLogger(dataSourceLoggerName);
    }

    public void setDataSourceLogger(Logger dataSourceLogger) {
        this.dataSourceLogger = dataSourceLogger;
        this.dataSourceLoggerName = dataSourceLogger.getName();
    }

    public String getConnectionLoggerName() {
        return this.connectionLoggerName;
    }

    public void setConnectionLoggerName(String connectionLoggerName) {
        this.connectionLoggerName = connectionLoggerName;
        this.connectionLogger = LogManager.getLogger(connectionLoggerName);
    }

    public void setConnectionLogger(Logger connectionLogger) {
        this.connectionLogger = connectionLogger;
        this.connectionLoggerName = connectionLogger.getName();
    }

    public String getStatementLoggerName() {
        return this.statementLoggerName;
    }

    public void setStatementLoggerName(String statementLoggerName) {
        this.statementLoggerName = statementLoggerName;
        this.statementLogger = LogManager.getLogger(statementLoggerName);
    }

    public void setStatementLogger(Logger statementLogger) {
        this.statementLogger = statementLogger;
        this.statementLoggerName = statementLogger.getName();
    }

    public String getResultSetLoggerName() {
        return this.resultSetLoggerName;
    }

    public void setResultSetLoggerName(String resultSetLoggerName) {
        this.resultSetLoggerName = resultSetLoggerName;
        this.resultSetLogger = LogManager.getLogger(resultSetLoggerName);
    }

    public void setResultSetLogger(Logger resultSetLogger) {
        this.resultSetLogger = resultSetLogger;
        this.resultSetLoggerName = resultSetLogger.getName();
    }

    public boolean isConnectionLogErrorEnabled() {
        return this.connectionLogger.isErrorEnabled() && super.isConnectionLogErrorEnabled();
    }

    public boolean isDataSourceLogEnabled() {
        return this.dataSourceLogger.isDebugEnabled() && super.isDataSourceLogEnabled();
    }

    public boolean isConnectionLogEnabled() {
        return this.connectionLogger.isDebugEnabled() && super.isConnectionLogEnabled();
    }

    public boolean isStatementLogEnabled() {
        return this.statementLogger.isDebugEnabled() && super.isStatementLogEnabled();
    }

    public boolean isResultSetLogEnabled() {
        return this.resultSetLogger.isDebugEnabled() && super.isResultSetLogEnabled();
    }

    public boolean isResultSetLogErrorEnabled() {
        return this.resultSetLogger.isErrorEnabled() && super.isResultSetLogErrorEnabled();
    }

    public boolean isStatementLogErrorEnabled() {
        return this.statementLogger.isErrorEnabled() && super.isStatementLogErrorEnabled();
    }

    protected void connectionLog(String message) {
        if(StrUtil.isNotEmpty(message)){
            message = message.replaceAll("\n","");
            // message = message.replaceAll("\\{[^{}]*\\}", "");
        }
        this.connectionLogger.info(message);
    }

    protected void statementLog(String message) {
        if(StrUtil.isNotEmpty(message)){
            message = message.replaceAll("\n","");
        }
        boolean skip = SysLog.checkDbPrintException(message,true);
        if(!skip){
            this.statementLogger.info(message);
        }
    }

    protected void resultSetLog(String message) {
        if(StrUtil.isNotEmpty(message)){
            message = message.replaceAll("\n","");
        }
        this.resultSetLogger.info(message);
    }

    protected void resultSetLogError(String message, Throwable error) {
        this.resultSetLogger.error(message, error);
    }

    protected void statementLogError(String message, Throwable error) {
        String message1 = error.getMessage();
        boolean skip = SysLog.checkDbPrintException(message1,false);
        if(!skip){
            // 数据库报错使用这种形式的打印
            error.printStackTrace();
        }
        //this.statementLogger.error(message, error);
    }

    @Override
    public void resultSet_close(FilterChain chain, ResultSetProxy resultSet) throws SQLException {

        chain.resultSet_close(resultSet);
    }

    // --------------------覆盖结果集打印--------------------
    @Override
    public boolean resultSet_next(FilterChain chain, ResultSetProxy resultSet) throws SQLException {
        boolean b = chain.resultSet_next(resultSet);
        if(b){
            /*try {
                int currentRows = resultSet.getRow();
                if(currentRows == 6){
                    resultSetLog("has more rows.... but max println 5 row");
                }else if(currentRows<6){
                    StringBuffer buf = new StringBuffer();
                    *//*buf.append("{conn-");
                    buf.append(resultSet.getStatementProxy().getConnectionProxy().getId());
                    buf.append(", rs-");
                    buf.append(resultSet.getId());
                    buf.append("}");*//*
                    buf.append("<== Result: [");

                    ResultSetMetaData meta = resultSet.getMetaData();
                    for (int i = 0, size = meta.getColumnCount(); i < size; ++i) {
                        if (i != 0) {
                            buf.append(", ");
                        }
                        int columnIndex = i + 1;
                        int type = meta.getColumnType(columnIndex);

                        Object value;
                        if (type == Types.TIMESTAMP) {
                            value = resultSet.getTimestamp(columnIndex);
                        } else if (type == Types.BLOB) {
                            value = "<BLOB>";
                        } else if (type == Types.CLOB) {
                            value = "<CLOB>";
                        } else if (type == Types.NCLOB) {
                            value = "<NCLOB>";
                        } else if (type == Types.BINARY) {
                            value = "<BINARY>";
                        } else {
                            value = resultSet.getObject(columnIndex);
                        }
                        buf.append(value);
                    }

                    buf.append("]");

                    resultSetLog(buf.toString());
                }
            } catch (SQLException ex) {
                resultSetLogError("logging error", ex);
            }*/
        }else{
            // print current cursor index
            resultSetLog("<== all rows is "+ resultSet.getCursorIndex());
        }

        return b;
    }


    // --------------------覆盖结果集打印--------------------
    @Override
    protected void resultSetOpenAfter(ResultSetProxy resultSet) {
        /*try {
            StringBuffer buf = new StringBuffer();
            *//*buf.append("{conn-");
            buf.append(resultSet.getStatementProxy().getConnectionProxy().getId());
            buf.append(", rs-");
            buf.append(resultSet.getId());
            buf.append("}");*//*

            buf.append("<== open Header: [");

            ResultSetMetaData meta = resultSet.getMetaData();
            for (int i = 0, size = meta.getColumnCount(); i < size; ++i) {
                if (i != 0) {
                    buf.append(", ");
                }
                buf.append(meta.getColumnName(i + 1));
            }
            buf.append("]");

            resultSetLog(buf.toString());
        } catch (SQLException ex) {
            resultSetLogError("logging error", ex);
        }*/
    }
}
