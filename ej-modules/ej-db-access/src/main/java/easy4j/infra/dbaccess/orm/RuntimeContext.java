package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Connection;
import java.util.*;
import java.util.function.Function;

/**
 * 这是运行时包装类
 *
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


    // 更新跳过null值
    private boolean isSkipNull;

    // 查Map结果是否转为驼峰
    private boolean resultFieldToCamel;

    // 对象类型
    private Class<T> clazz;

    // 主键获取函数
    private Function<T, Serializable> idget;

    // 查询的字段
    private List<String> selectFields;

    // 转义的查询字段
    private List<String> escapeSelectFields;


    // 工具类
    private AccessUtils accessUtils;

    // 所有字段信息
    private List<AccessField> columnInfoList;

    // 要更新的字段 以group字段来区分条数
    private List<AccessField> updateFields;

    // 要写入的字段列表 以group字段来区分条数
    private List<AccessField> insertFields;


    // 数据库类型
    private String dbType;

    // 请求传参
    private Access<T> access;

    // 最后追加的sql
    private String lastSql = "";

    // 分页传参
    private Page<T> page;

    // 是否返回map
    private boolean returnMap;

    private List<EasyMap<String, Object>> resultMapList;
    private List<T> resultList;
    private int effectRows;
    private long count;
    private boolean exists;

    public String getDotTableName() {
        return ListTs.join(SP.DOT, ListTs.asList(schema, tableName));
    }

    public List<AccessField> getColumnInfoList(List<AccessField> candidateList) {
        Map<Integer, List<AccessField>> integerListMap = ListTs.groupBy(candidateList, AccessField::getGroup);
        Set<Map.Entry<Integer, List<AccessField>>> entries = integerListMap.entrySet();
        Map.Entry<Integer, List<AccessField>> integerListEntry = ListTs.get(entries, 0);
        if (integerListEntry != null) {
            List<AccessField> value = integerListEntry.getValue();
            for (AccessField accessField : value) {
                accessField.setColumnValue(null);
            }
            return value;
        }
        return new ArrayList<>();

    }

    public List<Object> getArgs() {
        List<Object> args = new LinkedList<>();
        if (operateType == OperateType.SELECT) {
            ListTs.addAll(args, whereArgs);
        } else if (operateType == OperateType.SELECT_COUNT) {
            ListTs.addAll(args, whereArgs);
        } else if (operateType == OperateType.SELECT_EXIST) {
            ListTs.addAll(args, whereArgs);
        } else if (operateType == OperateType.SELECT_PAGE) {
            ListTs.addAll(args, whereArgs);
        } else if (operateType == OperateType.INSERT) {
            for (AccessField insertField : insertFields) {
                ListTs.add(args, insertField.getColumnValue());
            }
        } else if (operateType == OperateType.UPDATE) {
            for (AccessField u : updateFields) {
                ListTs.add(args, u.getColumnValue());
            }
            ListTs.addAll(args, whereArgs);

        } else if (operateType == OperateType.DELETE) {
            ListTs.addAll(args, whereArgs);
        }
        return args;
    }


}
