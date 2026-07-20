package easy4j.infra.dbaccess.orm;

import cn.hutool.core.collection.CollUtil;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.orm.runner.LogResult;
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

    // 查询的字段
    private List<String> selectFields;

    // 转义的查询字段
    private List<String> escapeSelectFields;


    // 工具类
    private AccessUtils accessUtils;

    // 所有字段信息
    private List<AccessField> columnInfoList;

    // 主键值
    private List<AccessField> idList;

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

    // 执行结果
    private LogResult logResult;

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
        if (
                operateType == OperateType.SELECT ||
                        operateType == OperateType.SELECT_COUNT ||
                        operateType == OperateType.SELECT_EXIST ||
                        operateType == OperateType.SELECT_PAGE
        ) {
            ListTs.addAll(args, whereArgs);
        } else if (operateType == OperateType.INSERT) {
            Map<String, List<AccessField>> integerListMap = ListTs.groupBy(insertFields, e -> String.valueOf(e.getGroup()));
            Set<Map.Entry<String, List<AccessField>>> entries = integerListMap.entrySet();
            for (Map.Entry<String, List<AccessField>> entry : entries) {
                List<AccessField> value = entry.getValue();
                for (AccessField insertField : value) {
                    ListTs.add(args, insertField.getColumnValue());
                }
            }
        } else if (operateType == OperateType.UPDATE) {
            // update暂且不弄批量更新 目前是循环更新 所以不用分组
            for (AccessField u : updateFields) {
                ListTs.add(args, u.getColumnValue());
            }
            ListTs.addAll(args, whereArgs);

        } else if (operateType == OperateType.DELETE) {
            ListTs.addAll(args, whereArgs);
        }
        Access<T> access1 = getAccess();
        if (access1 != null) {
            List<Object> args1 = access1.getArgs();
            if (CollUtil.isNotEmpty(args1)) {
                ListTs.addAll(args, args1);
            }
        }
        return args;
    }


    public AccessConfig getConfig() {
        AccessConfig accessConfig = null;
        AccessUtils accessUtils1 = this.getAccessUtils();
        if (accessUtils1 != null) {
            accessConfig = accessUtils1.getAccessConfig();
        }
        if (accessConfig == null) {
            accessConfig = new AccessConfig();
        }
        return accessConfig;
    }

}
