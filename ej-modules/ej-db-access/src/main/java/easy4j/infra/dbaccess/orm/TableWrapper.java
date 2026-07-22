package easy4j.infra.dbaccess.orm;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.orm.conditions.WhereBuild;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class TableWrapper {

    @Getter
    private final List<TableItem> tableItemList = ListTs.newLinkedList();

    WhereBuild whereBuild;

    public TableWrapper(TableItem ...tableItem) {
        tableItemList.addAll(Arrays.asList(tableItem));
    }

    public TableWrapper where(WhereBuild whereBuild){
        this.whereBuild = whereBuild;
        return this;
    }

    public String build(){
        // TODO
        StringBuilder stringBuilder = new StringBuilder();
        for (TableItem tableItem : tableItemList) {
            String name = tableItem.getName();
            String[] pickArgs = tableItem.getPickArgs();
        }
        return stringBuilder.toString();
    }

}
