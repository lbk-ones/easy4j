package easy4j.infra.dbaccess.orm;

import easy4j.infra.dbaccess.domain.OperationLogs;
import easy4j.infra.dbaccess.orm.conditions.FWhereBuild;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import lombok.Data;

/**
 *
 * @author bokun.li
 */
@Data
public class TableItem {

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

    private TableItem() {
        this.joinSymbol = false;
    }

    // ============================================================================

    public static TableItem leftJoin() {
        TableItem tableItem = new TableItem();
        tableItem.setJoinSymbol(true);
        tableItem.setJoin("left join");
        return tableItem;
    }

    public static TableItem rightJoin(String name) {
        TableItem tableItem = new TableItem();
        tableItem.setJoinSymbol(true);
        tableItem.setJoin("right join");
        return tableItem;
    }

    public static TableItem join() {
        TableItem tableItem = new TableItem();
        tableItem.setJoinSymbol(true);
        tableItem.setJoin("join");
        return tableItem;
    }

    public static TableItem join(String name) {
        TableItem tableItem = new TableItem();
        tableItem.setJoinSymbol(true);
        tableItem.setJoin(name);
        return tableItem;
    }

    // ============================================================================



    public static TableItem of(String on, Class<?> clazz, String... pickArgs) {
        TableItem tableItem = new TableItem();
        tableItem.setClazz(clazz);
        tableItem.setOn(on);
        tableItem.setPickArgs(pickArgs);
        return tableItem;
    }

    public static TableItem ofName(String name, String on, Class<?> clazz, String... pickArgs) {
        TableItem tableItem = of(on, clazz, pickArgs);
        tableItem.setName(name);
        return tableItem;
    }

    public static TableItem of(Class<?> clazz, String... pickArgs) {
        TableItem tableItem = new TableItem();
        tableItem.setClazz(clazz);
        if (pickArgs.length == 0) {
            tableItem.setPickArgs(new String[]{"*"});
        } else {
            tableItem.setPickArgs(pickArgs);
        }
        return tableItem;
    }

    public static TableItem ofName(String name, Class<?> clazz, String... pickArgs) {
        TableItem tableItem = of(clazz, pickArgs);
        tableItem.setName(name);
        return tableItem;
    }

    public static TableItem of(WhereBuild whereBuild, Class<?> clazz, String... pickArgs) {
        TableItem tableItem = of(clazz, pickArgs);
        tableItem.setWhereBuild(whereBuild);
        return tableItem;
    }

    public static TableItem ofName(String name, WhereBuild whereBuild, Class<?> clazz, String... pickArgs) {
        TableItem tableItem = of(clazz, pickArgs);
        tableItem.setWhereBuild(whereBuild);
        tableItem.setName(name);
        return tableItem;
    }

    public static void main(String[] args) {
        String build = new TableWrapper(
                TableItem.ofName("a", null),
                TableItem.leftJoin(),
                TableItem.ofName("b", null)
        ).where(
                // #1 可以是 a 如果写#1 则代表拿第一个名称
                FWhereBuild.get(OperationLogs.class).sql("#1.xxx = 123 and #2.www is not null")
        ).build();
        System.out.println(build);
    }

}