package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.function.Function;

/**
 * 这是运行时包装类
 * @param <T>
 */
@Data
@Accessors(chain = true)
public class RuntimeContext<T> {

    // 传进来的对象集合，如果是单个则数量为1
    private List<T> params;

    // 连接对象
    private Connection connection;

    // 数据库方言
    private DialectV2 dialectV2;

    // 操作类型
    private OperateType operateType;

    // 最后的sql
    private String sql;

    // where后面的sql
    private String whereSql;

    // where后面sql的参数值
    private List<Object> whereArgs;

    // 表名
    private String tableName;

    // schema名称
    private String schema;

    // 参数列表
    private List<Object> updateArgs;


    // 更新跳过null值
    private boolean isSkipNull;

    // 查Map结果是否转为驼峰
    private boolean resultFieldToCamel;

    // 对象类型
    private Class<T> clazz;

    // 主键获取函数
    private Function<T, Serializable> idget;

    // 字段信息
    private List<AccessField> columns;

    // 查询的字段
    private List<String> selectFields;


    // 工具类
    private AccessUtils accessUtils;

    // 要更新的字段
    private List<AccessField> updateFields;


    // 数据库类型
    private String dbType;

    // 请求传参
    private Access<T> access;

    // 最后追加的sql
    private String lastSql = "";


    private Page<T> page;


}
