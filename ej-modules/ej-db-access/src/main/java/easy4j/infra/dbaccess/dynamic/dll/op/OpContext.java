package easy4j.infra.dbaccess.dynamic.dll.op;

import easy4j.infra.common.annotations.Desc;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

@Data
@Accessors(chain = true)
public class OpContext {

    // 数据库类型
    private String dbType;

    // 数据库版本
    private String dbVersion;

    // mysql拿catalog当schema 其他数据库不一定是这个
    private String connectionCatalog;

    // 这个不一定有值 看驱动实现
    private String connectionSchema;

    // 数据库中已有的列信息
    private List<DynamicColumn> dbColumns;

    // 数据库中已有的列信息
    @Desc("需要新增的列")
    private List<DDLFieldInfo> adColumns;

    // 传入的schema信息
    private String schema;

    // 表名称
    private String tableName;

    // 传入的数据源信息
    private DataSource dataSource;

    // 获取的全局连接
    private Connection connection;

    // 解析出来的数据库方言
    private Dialect dialect;

    // 解析出来的表元数据
    private DDLTableInfo ddlTableInfo;

    // parse java 才有这个字段
    private Class<?> domainClass;

    // 全局配置
    private OpConfig opConfig;


}
