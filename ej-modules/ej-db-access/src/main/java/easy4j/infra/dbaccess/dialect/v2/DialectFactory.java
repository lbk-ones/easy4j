package easy4j.infra.dbaccess.dialect.v2;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dialect.v2.impl.*;
import easy4j.infra.dbaccess.helper.JdbcHelper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 获取方言的入口
 *
 * @author bokun.li
 * @date 2025/10/13
 */
public class DialectFactory {

    private static final Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

    private static Properties getDefaultDatabaseTypeMappings() {
        Properties databaseTypeMappings = new Properties();
        databaseTypeMappings.setProperty("H2", DbType.H2.getDb());
        databaseTypeMappings.setProperty("MySQL", DbType.MYSQL.getDb());
        databaseTypeMappings.setProperty("Oracle", DbType.ORACLE.getDb());
        databaseTypeMappings.setProperty("PostgreSQL", DbType.POSTGRE_SQL.getDb());
        databaseTypeMappings.setProperty("Microsoft SQL Server", DbType.SQL_SERVER.getDb());
        databaseTypeMappings.setProperty("DB2", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/NT", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/NT64", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2 UDP", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/LINUX", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/LINUX390", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/LINUXX8664", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/LINUXZ64", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/400 SQL", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/6000", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2 UDB iSeries", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/AIX64", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/HPUX", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/HP64", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/SUN", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/SUN64", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/PTX", DbType.DB2.getDb());
        databaseTypeMappings.setProperty("DB2/2", DbType.DB2.getDb());
        return databaseTypeMappings;
    }


    public static DialectV2 get(Connection connection) {
        DialectV2 resDialect = null;
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            String dbType = databaseTypeMappings.getProperty(databaseProductName);
            if (StrUtil.isEmpty(dbType)) {
                throw new IllegalArgumentException("the dbType is empty " + dbType);
            }
            if (DbType.MYSQL.getDb().equals(dbType)) {
                resDialect = new MysqlDialect(connection);
            } else if (DbType.POSTGRE_SQL.getDb().equals(dbType)) {
                resDialect = new PostgresqlDialect(connection);
            } else if (DbType.ORACLE.getDb().equals(dbType)) {
                resDialect = new OracleDialect(connection);
            } else if (DbType.SQL_SERVER.getDb().equals(dbType)) {
                resDialect = new SQLServerDialect(connection);
            } else if (DbType.H2.getDb().equals(dbType)) {
                resDialect = new H2Dialect(connection);
            } else if (DbType.DB2.getDb().equals(dbType)) {
                resDialect = new DB2Dialect(connection);
            }
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("get dialect", null, e);
        }

        return resDialect;
    }

}
