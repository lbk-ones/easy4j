package easy4j.infra.dbaccess.orm.sql;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.domain.OperationLogs;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import easy4j.infra.dbaccess.orm.*;
import easy4j.infra.dbaccess.orm.conditions.FWhereBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 处理复杂联表查询
 *
 * @author bokun.li
 */
public class JoinSql implements ISql {

    @Override
    public <T> boolean match(RuntimeContext<T> runtimeContext) {
        return runtimeContext.getOperateType() == OperateType.SELECT_JOIN;
    }

    @Override
    public <T> String build(RuntimeContext<T> runtimeContext) {
        List<Object> sqlAllArgs = new ArrayList<>();
        AccessUtils accessUtils = runtimeContext.getAccessUtils();
        DialectV2 dialectV2 = runtimeContext.getDialectV2();
        SqlWrapper sqlWrapper = runtimeContext.getSqlWrapper();
        List<SqlItem> sqlItemList = sqlWrapper.getSqlItemList();
        // 1、检查，并收集排除的name
        SqlItem last = null;
        Set<String> excludeName = new HashSet<>();
        boolean error = false;
        for (SqlItem sqlItem : sqlItemList) {
            if (!sqlItem.isJoinSymbol()) {
                String name1 = sqlItem.getName();
                if (StrUtil.isNotBlank(name1)) {
                    excludeName.add(name1);
                }
            }
            if (last != null && !error) {
                if (!last.isJoinSymbol() && !sqlItem.isJoinSymbol()) {
                    error = true;
                }
            }
            last = sqlItem;
        }
        if (error) {
            throw new AccessException("Join sql , The parameters were not passed in the specified format");
        }
        List<SqlItem> sqlItems = new ArrayList<>();
        // 2、填充name
        LetterProvider letterProvider = new LetterProvider(excludeName);
        for (SqlItem sqlItem : sqlItemList) {
            if (!sqlItem.isJoinSymbol()) {
                String name = sqlItem.getName();
                if (StrUtil.isBlank(name)) {
                    String next = letterProvider.next();
                    sqlItem.setName(next);
                }
                sqlItems.add(sqlItem);
            }
        }
        letterProvider.reset();
        String[] allArgs = {};
        // 3、处理参数
        for (SqlItem sqlItem : sqlItems) {
            String name = sqlItem.getName();
            String[] pickArgs = sqlItem.getPickArgs();
            if (pickArgs != null && pickArgs.length > 0) {
                String[] args = {};
                for (String pickArg : pickArgs) {
                    String suffix = "";
                    String pickArg_ = pickArg;
                    // 处理别名 as 或者空格
                    int asIndex = pickArg_.indexOf(" as ");
                    if (asIndex > 0) {
                        List<String> split = StrUtil.split(pickArg_, "as");
                        if (split.size() == 2) {
                            suffix = " as " + StrUtil.trim(split.get(1));
                            pickArg_ = StrUtil.trim(split.get(0));
                        }
                    } else {
                        int eIndex = pickArg_.indexOf(" ");
                        if (eIndex > 0) {
                            List<String> split = StrUtil.split(pickArg_, " ");
                            split = split.stream().filter(StrUtil::isNotBlank).map(StrUtil::trim).toList();
                            suffix = " " + StrUtil.trim(split.get(1));
                            pickArg_ = StrUtil.trim(split.get(0));
                        }
                    }
                    // 转下划线
                    pickArg_ = accessUtils.fn(pickArg_);
                    if (!StrUtil.startWith(pickArg_, name + SP.DOT)) {
                        // 和 name 不一样 则去除前部分 重新添加name
                        int i = pickArg_.indexOf(SP.DOT);
                        if (i > 0) {
                            pickArg_ = StrUtil.sub(pickArg_, i + 1, pickArg_.length());
                        }
                        pickArg_ = name + SP.DOT + accessUtils.escapeCn(pickArg_, dialectV2, false);
                    } else {
                        int i = pickArg_.indexOf(SP.DOT);
                        // 拿到表别称
                        String name_ = StrUtil.sub(pickArg_, 0, i);
                        // 拿到字段名称
                        String fieldName = StrUtil.sub(pickArg_, i + 1, pickArg_.length());
                        // 重新拼接
                        pickArg_ = name_ + SP.DOT + accessUtils.escapeCn(fieldName, dialectV2, false);
                    }
                    pickArg_ = pickArg_ + suffix;
                    allArgs = ArrayUtil.append(allArgs, pickArg_);
                    args = ArrayUtil.append(args, pickArg_);
                }
                sqlItem.setPickArgs(args);
            }
            Class<?> clazz = sqlItem.getClazz();
            String tableName1 = sqlItem.getTableName();
            if (clazz != null && StrUtil.isBlank(tableName1)) {
                tableName1 = accessUtils.getTableName(clazz, dialectV2);
            }
            sqlItem.setTableName(tableName1);
            String on = sqlItem.getOn();
            if (StrUtil.isNotBlank(on)) {
                on = accessUtils.escapeCn(accessUtils.fn(on), dialectV2, false);
            }
            sqlItem.setOn(on);
        }

        StringBuilder sqlBuild = new StringBuilder("select");
        for (int i = 0; i < allArgs.length; i++) {
            String allArg = allArgs[i];
            if (i > 0) {
                sqlBuild.append(SP.SPACE);
                sqlBuild.append(SP.COMMA);
            }
            sqlBuild.append(SP.SPACE);
            sqlBuild.append(allArg);
        }
        sqlBuild.append(SP.SPACE);
        sqlBuild.append("from");
        // 4、开始拼接 from语句
        SqlItem one = null;
        int i = 1;
        // a join b join c join d
        // 1 3 5 7
        for (SqlItem sqlItem : sqlItemList) {
            String name = sqlItem.getName();
            if (!sqlItem.isJoinSymbol()) {
                sqlBuild.append(SP.SPACE);
                sqlBuild.append(sqlItem.getTableName());
                sqlBuild.append(SP.SPACE);
                sqlBuild.append(name);
            } else {
                sqlBuild.append(SP.SPACE);
                sqlBuild.append(sqlItem.getJoin());
            }
            if (one != null && !sqlItem.isJoinSymbol()) {
                if (i > 1 && i % 2 == 1) {
                    String lastName = one.getName();
                    String currentName = sqlItem.getName();
                    sqlBuild.append(SP.SPACE);
                    sqlBuild.append("on");
                    sqlBuild.append(SP.SPACE);
                    sqlBuild.append("(");
                    sqlBuild.append(SP.SPACE);
                    sqlBuild.append(lastName).append(SP.DOT).append(one.getOn());
                    sqlBuild.append(SP.SPACE);
                    sqlBuild.append("=");
                    sqlBuild.append(SP.SPACE);
                    sqlBuild.append(currentName).append(SP.DOT).append(sqlItem.getOn());
                    // 解析两个条件构造器
                    WhereBuild lastWhere = one.getWhereBuild();
                    WhereBuild current = sqlItem.getWhereBuild();
                    this.parseOnWhere(runtimeContext, sqlAllArgs, accessUtils, sqlBuild, lastName, lastWhere);
                    this.parseOnWhere(runtimeContext, sqlAllArgs, accessUtils, sqlBuild, currentName, current);
                    sqlBuild.append(SP.SPACE);
                    sqlBuild.append(")");
                }
            }
            // 1 3 5 7 9
            if (i % 2 == 1) {
                one = sqlItem;
            }
            i++;
        }
        // 4、开始拼接where语句
        String string = sqlBuild.toString();
        WhereBuild whereBuild = sqlWrapper.getWhereBuild();
        if (whereBuild != null) {
            accessUtils.parseWhere(whereBuild, runtimeContext);
            List<Object> whereArgs = runtimeContext.getWhereArgs();
            if (CollUtil.isNotEmpty(whereArgs)) {
                sqlAllArgs.addAll(whereArgs);
            }
            runtimeContext.setWhereArgs(sqlAllArgs);
            // 这里是不需要参数的
            runtimeContext.setSelectFields(new ArrayList<>());
            runtimeContext.setEscapeSelectFields(new ArrayList<>());
            string = accessUtils.appendWhere(string, runtimeContext.getWhereSql());
        }
        return string;
    }

    /**
     * 解析条件到 on语句里面
     *
     * @param runtimeContext 上下文
     * @param sqlAllArgs     所有参数
     * @param accessUtils    工具类
     * @param sqlBuild       sql拼接器
     * @param namePrefix     前缀
     * @param where          要解析的条件构造器
     * @param <T>            泛型约束
     */
    private <T> void parseOnWhere(RuntimeContext<T> runtimeContext, List<Object> sqlAllArgs, AccessUtils accessUtils, StringBuilder sqlBuild, String namePrefix, WhereBuild where) {
        if (where != null) {
            accessUtils.clearWhere(runtimeContext);
            runtimeContext.setArgNamePrefix(namePrefix);
            accessUtils.parseWhere(where, runtimeContext);
            sqlBuild.append(SP.SPACE);
            sqlBuild.append("and");
            sqlBuild.append(SP.SPACE);
            sqlBuild.append(("("));
            sqlBuild.append(SP.SPACE);
            sqlBuild.append(runtimeContext.getWhereSql());
            sqlBuild.append(SP.SPACE);
            sqlBuild.append((")"));
            List<Object> whereArgs = runtimeContext.getWhereArgs();
            if (whereArgs != null && !whereArgs.isEmpty()) {
                sqlAllArgs.addAll(whereArgs);
            }
            accessUtils.clearWhere(runtimeContext);
        }
    }


    /**
     * 测试用例
     */
    private static void test() {

        String jdbcUrl = "jdbc:h2:mem:testdb";
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        DataSource dataSource = new TempDataSource(driverClassNameByUrl, jdbcUrl, "sa", "");

        JoinSql joinSql = new JoinSql();
        Access<SysLogRecord> tAccess = new Access<SysLogRecord>()
                .setClazz(SysLogRecord.class)
                .setOperateType(OperateType.SELECT_JOIN);
        AccessConfig accessConfig = new AccessConfig();
        accessConfig.setDataSource(dataSource);
        AccessUtils accessUtils = new AccessUtils(accessConfig);
        RuntimeContext<SysLogRecord> context = accessUtils.toContext(tAccess);

        context.setSqlWrapper(
                new SqlWrapper(
                        SqlItem.of(OperationLogs::getOperatorId, OperationLogs.class,
                                FWhereBuild.get(OperationLogs.class)
                                        .eq(OperationLogs::getAction, "323")
                                        .inArray(OperationLogs::getId, 1L, 2L),
                                OperationLogs::getOperatorId, OperationLogs::getOperatorName
                        ),
                        SqlItem.leftJoin(),
                        SqlItem.of("id", SysLogRecord.class),
                        SqlItem.rightJoin(),
                        SqlItem.of("var1", "", OperationLogs.class, "var1", "var2"),
                        SqlItem.join(),
                        SqlItem.of("operateCode", SysLogRecord.class, "var3", "var4"),
                        SqlItem.join("hash join"),
                        SqlItem.of("operateCode2", SysLogRecord.class, "var5  varxx", "wq.var6  as  wqx")
                ).where(FWhereBuild.get(SysLogRecord.class).sql(true, "a.operateId = ? and b.tag = ?", "23", "25"))
        );
        String build = joinSql.build(context);
        System.out.println(build);
        accessUtils.releaseConnection(context);

    }
}
