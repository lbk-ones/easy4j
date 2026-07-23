package easy4j.infra.dbaccess.orm;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import lombok.Data;
import org.bouncycastle.util.Arrays;

/**
 *
 * @author bokun.li
 */
@Data
public class SqlItem {

    private Class<?> clazz;

    // 拿出来on的字段
    private String on;

    private String[] pickArgs;

    // left join , join
    private String join;

    private WhereBuild whereBuild;

    private boolean joinSymbol;

    // 占位符 a,b,c 最终会解析成 a.xxx b.xxx
    private String name;

    private String tableName;

    private SqlItem() {
        this.joinSymbol = false;
    }

    // ============================================================================

    public static SqlItem leftJoin() {
        SqlItem sqlItem = new SqlItem();
        sqlItem.setJoinSymbol(true);
        sqlItem.setJoin("left join");
        return sqlItem;
    }

    public static SqlItem rightJoin() {
        SqlItem sqlItem = new SqlItem();
        sqlItem.setJoinSymbol(true);
        sqlItem.setJoin("right join");
        return sqlItem;
    }

    public static SqlItem join() {
        SqlItem sqlItem = new SqlItem();
        sqlItem.setJoinSymbol(true);
        sqlItem.setJoin("join");
        return sqlItem;
    }

    public static SqlItem join(String name) {
        SqlItem sqlItem = new SqlItem();
        sqlItem.setJoinSymbol(true);
        sqlItem.setJoin(name);
        return sqlItem;
    }

    private static <T> String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }

    @SafeVarargs
    private static <T> String[] picks(Func1<T, ?>... pickArgs) {
        String[] pick = {};
        for (Func1<T, ?> pickArg : pickArgs) {
            String name1 = getName(pickArg);
            pick = Arrays.append(pick, name1);
        }
        return pick;
    }
    // ============================================================================

    private static SqlItem of(Class<?> clazz, String... pickArgs) {
        SqlItem sqlItem = new SqlItem();
        sqlItem.setClazz(clazz);
        if (pickArgs.length == 0) {
            sqlItem.setPickArgs(new String[]{"*"});
        } else {
            sqlItem.setPickArgs(pickArgs);
        }
        return sqlItem;
    }

    private static SqlItem ofTable(String tableName, String... pickArgs) {
        SqlItem sqlItem = new SqlItem();
        sqlItem.setTableName(tableName);
        if (pickArgs.length == 0) {
            sqlItem.setPickArgs(new String[]{"*"});
        } else {
            sqlItem.setPickArgs(pickArgs);
        }
        return sqlItem;
    }


    /**
     * 条件构造，没有name会自动推算
     *
     * @param on       需要联表查询的那个字段
     * @param clazz    类对象
     * @param pickArgs 查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    public static SqlItem of(String on, Class<?> clazz, String... pickArgs) {
        SqlItem sqlItem = of(clazz, pickArgs);
        sqlItem.setOn(on);
        return sqlItem;
    }

    /**
     * 条件构造，没有name会自动推算
     *
     * @param on         需要联表查询的那个字段
     * @param clazz      类对象
     * @param whereBuild on里面的条件构造器 会追加到 on语句里面 比如 on (a.aid = b.bid and (追加到这里))，会自动添加这个表的别名 比如 a b c
     * @param pickArgs   查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    public static SqlItem of(String on, Class<?> clazz, WhereBuild whereBuild, String... pickArgs) {
        SqlItem sqlItem = of(on, clazz, pickArgs);
        sqlItem.setWhereBuild(whereBuild);
        return sqlItem;
    }

    /**
     * 条件构造，没有name会自动推算
     *
     * @param on       需要联表查询的那个字段
     * @param name     表名别称 比如 a,b,c
     * @param clazz    类对象
     * @param pickArgs 查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    public static SqlItem of(String on, String name, Class<?> clazz, String... pickArgs) {
        SqlItem sqlItem = of(on, clazz, pickArgs);
        sqlItem.setName(name);
        return sqlItem;
    }

    /**
     * 条件构造，没有name会自动推算
     *
     * @param on         需要联表查询的那个字段
     * @param name       表名别称 比如 a,b,c
     * @param clazz      类对象
     * @param whereBuild on里面的条件构造器 会追加到 on语句里面 比如 on (a.aid = b.bid and (追加到这里))，会自动添加这个表的别名 比如 a b c
     * @param pickArgs   查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    public static SqlItem of(String on, String name, Class<?> clazz, WhereBuild whereBuild, String... pickArgs) {
        SqlItem sqlItem = of(on, name, clazz, pickArgs);
        sqlItem.setWhereBuild(whereBuild);
        return sqlItem;
    }

    /**
     * 条件构造(lambda)，没有name会自动推算
     *
     * @param on       需要联表查询的那个字段
     * @param clazz    类对象
     * @param pickArgs 查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    @SafeVarargs
    public static <T> SqlItem of(Func1<T, ?> on, Class<?> clazz, Func1<T, ?>... pickArgs) {
        return of(getName(on), clazz, picks(pickArgs));
    }

    /**
     * 条件构造(lambda)，没有name会自动推算
     *
     * @param on       需要联表查询的那个字段
     * @param name     表名别称 比如 a,b,c
     * @param clazz    类对象
     * @param pickArgs 查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    @SafeVarargs
    public static <T> SqlItem of(Func1<T, ?> on, String name, Class<?> clazz, Func1<T, ?>... pickArgs) {
        SqlItem sqlItem = of(on, clazz, pickArgs);
        sqlItem.setName(name);
        return sqlItem;
    }

    /**
     * 条件构造(lambda)，没有name会自动推算
     *
     * @param on         需要联表查询的那个字段
     * @param clazz      类对象
     * @param whereBuild on里面的条件构造器 会追加到 on语句里面 比如 on (a.aid = b.bid and (追加到这里))，会自动添加这个表的别名 比如 a b c
     * @param pickArgs   查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    @SafeVarargs
    public static <T> SqlItem of(Func1<T, ?> on, Class<?> clazz, WhereBuild whereBuild, Func1<T, ?>... pickArgs) {
        SqlItem sqlItem = of(on, clazz, pickArgs);
        sqlItem.setWhereBuild(whereBuild);
        return sqlItem;
    }

    /**
     * 条件构造(lambda)，没有name会自动推算
     *
     * @param on         需要联表查询的那个字段
     * @param name       表名别称 比如 a,b,c
     * @param clazz      类对象
     * @param whereBuild on里面的条件构造器 会追加到 on语句里面 比如 on (a.aid = b.bid and (追加到这里))，会自动添加这个表的别名 比如 a b c
     * @param pickArgs   查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    @SafeVarargs
    public static <T> SqlItem of(Func1<T, ?> on, String name, Class<?> clazz, WhereBuild whereBuild, Func1<T, ?>... pickArgs) {
        SqlItem sqlItem = of(on, name, clazz, pickArgs);
        sqlItem.setWhereBuild(whereBuild);
        return sqlItem;
    }


    /**
     * 条件构造直接指定表名，没有name会自动推算
     *
     * @param on        需要联表查询的那个字段
     * @param tableName 表名
     * @param pickArgs  查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    public static SqlItem ofTable(String on, String tableName, String... pickArgs) {
        SqlItem sqlItem = ofTable(tableName, pickArgs);
        sqlItem.setOn(on);
        return sqlItem;
    }

    /**
     * 条件构造直接指定表名，没有name会自动推算
     *
     * @param on         需要联表查询的那个字段
     * @param tableName  表名
     * @param whereBuild on里面的条件构造器 会追加到 on语句里面 比如 on (a.aid = b.bid and (追加到这里))，会自动添加这个表的别名 比如 a b c
     * @param pickArgs   查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    public static SqlItem ofTable(String on, String tableName, WhereBuild whereBuild, String... pickArgs) {
        SqlItem sqlItem = ofTable(on, tableName, pickArgs);
        sqlItem.setWhereBuild(whereBuild);
        return sqlItem;
    }

    /**
     * 条件构造 直接指定表名，没有name会自动推算
     *
     * @param on         需要联表查询的那个字段
     * @param tableName  表名
     * @param name       表别称
     * @param whereBuild on里面的条件构造器 会追加到 on语句里面 比如 on (a.aid = b.bid and (追加到这里))，会自动添加这个表的别名 比如 a b c
     * @param pickArgs   查询的参数可以加name比如 a.xxx、b.xx 也可以不加，同样也支持别名 name1 as name、name2 na
     * @return TableItem
     */
    public static SqlItem ofTable(String on, String tableName, String name, WhereBuild whereBuild, String... pickArgs) {
        SqlItem sqlItem = ofTable(on, tableName, whereBuild, pickArgs);
        sqlItem.setName(name);
        return sqlItem;
    }
}