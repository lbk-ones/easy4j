package easy4j.infra.dbaccess.dynamic.dll;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import com.baomidou.mybatisplus.annotation.TableName;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.dll.ct.DDLParseExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.DdlCtClassExecutor;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.InformationSchema;
import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.Table;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 动态ddl解析，从java实体逆向到数据库
 *
 * @author bokun.li
 * @date 2025-08-03
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DDLParseJavaClass extends CommonDBAccess implements DDLParse {

    DDLConfig dllConfig;

    /**
     * @param aClass     传入实体类
     * @param dataSource 传入数据源
     * @param schema     传入schema
     */
    public DDLParseJavaClass(Class<?> aClass, DataSource dataSource, String schema) {
        CheckUtils.notNull(aClass, "aClass");
        CheckUtils.notNull(dataSource, "dataSource");
        mount(aClass, dataSource, schema);

    }


    public void mount(Class<?> aClass, DataSource dataSource, String schema) {
        Connection connection = null;
        String ddl = null;
        boolean hasException = false;
        try {
            this.dllConfig = new DDLConfig();
            connection = dataSource.getConnection();
            Dialect dialect = JdbcHelper.getDialect(connection);
            String dbType = InformationSchema.getDbType(dataSource, connection);
            String dbVersion = InformationSchema.getDbVersion(dataSource, connection);
            String ddlTableName = getDDLTableName(dialect,aClass, getTableName(aClass, dialect));
            List<DynamicColumn> columns = InformationSchema.getColumns(dataSource, schema, ddlTableName, connection);
            this.dllConfig.setDataSource(dataSource)
                    .setConnection(connection)
                    .setSchema(schema)
                    .setTableName(ddlTableName)
                    .setDbType(dbType)
                    .setDbVersion(dbVersion)
                    .setDbColumns(columns)
                    .setDialect(dialect)
                    .setDomainClass(aClass);

        } catch (SQLException sqlE) {
            hasException = true;
            throw JdbcHelper.translateSqlException("", ddl, sqlE);
        } catch (Exception e) {
            hasException = true;
            throw e;
        } finally {
            if (hasException) {
                JdbcHelper.close(connection);
            }
        }
    }


    // 创建表sql
    public String getCreateTableTxt() {
        DDLParseExecutor ddlCtExecutor = new DdlCtClassExecutor(this.dllConfig);
        return ddlCtExecutor.execute();
    }

    // 新增字段sql
    public String getAddColumnTxt(Class<?> aclass) {

        return "";
    }

    public String getDDLTxt() {

        if (CollUtil.isNotEmpty(this.dllConfig.getDbColumns())) {
            return getAddColumnTxt(this.dllConfig.getDomainClass());
        } else {
            return getCreateTableTxt();
        }
    }

    public void execDDL() {
        String ddl = null;
        try {
            ddl = getDDLTxt();
            DDlHelper.execDDL(this.dllConfig.getConnection(), ddl, null, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDDLFragment() {
        String ddl = null;
        try {
            ddl = getDDLTxt();
        } finally {
            JdbcHelper.close(this.dllConfig.getConnection());
        }
        return ddl;
    }

    private String getDDLTableName(Dialect dialect,Class<?> aclass, String tableName) {
        boolean annotationPresent = aclass.isAnnotationPresent(DDLTable.class);
        if (annotationPresent) {
            Wrapper wrapper = dialect.getWrapper();
            try{
                char preWrapQuote = wrapper.getPreWrapQuote();
                char sufWrapQuote = wrapper.getSufWrapQuote();
                tableName = StrUtil.unWrap(tableName,preWrapQuote,sufWrapQuote);
            }catch (Exception ignored){
            }

            DDLTable annotation = aclass.getAnnotation(DDLTable.class);
            String s = annotation.tableName();
            String fName;
            if (StrUtil.isNotBlank(s)) {
                fName = s;
            } else {
                fName = tableName;
            }
            if (this.dllConfig.isToUnderLine()) {
                fName = toUnderLine(fName);
            }
            return fName;
        }else{
            if(aclass.isAnnotationPresent(JdbcTable.class)){
                JdbcTable annotation = aclass.getAnnotation(JdbcTable.class);
                String name = annotation.name();
                if(StrUtil.isNotBlank(name)) return name;
            }else if(aclass.isAnnotationPresent(TableName.class)){
                TableName tableName1 = aclass.getAnnotation(TableName.class);
                String name = tableName1.value();
                if(StrUtil.isNotBlank(name)) return name;
            }else if(aclass.isAnnotationPresent(Table.class)){
                Table table = aclass.getAnnotation(Table.class);
                String name = table.name();
                if(StrUtil.isNotBlank(name)) return name;
            }
            String simpleName = aclass.getSimpleName();
            return StrUtil.toUnderlineCase(simpleName);
        }
//        return null;
    }


}
