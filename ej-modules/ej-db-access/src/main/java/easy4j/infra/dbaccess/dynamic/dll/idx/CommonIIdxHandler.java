package easy4j.infra.dbaccess.dynamic.dll.idx;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;

import java.util.List;
import java.util.stream.Collectors;

public class CommonIIdxHandler implements IIdxHandler {

    @Override
    public boolean match(DDLIndexInfo ddlIndexInfo) {
        return false;
    }

    @Override
    public String getName() {
        return "common";
    }

    @Override
    public String getIdx(DDLIndexInfo ddlIndexInfo) {
        String name = ddlIndexInfo.getName();
        String[] keys = ddlIndexInfo.getKeys();
        CheckUtils.checkTrue(keys == null || keys.length == 0, "keys not allow be empty");
        String indexTypeName = StrUtil.blankToDefault(ddlIndexInfo.getIndexTypeName(), "");
        if (StrUtil.isBlank(name)) {
            if (keys != null && keys.length > 0) {
                String lowerCase = indexTypeName.toLowerCase();
                String collect = ListTs.asList(keys)
                        .stream()
                        .map(StrUtil::toUnderlineCase)
                        .collect(Collectors.joining("_"));
                name = (StrUtil.isBlank(lowerCase) ? "" : (lowerCase + "_")) + "idx_" + ddlIndexInfo.getTableName().toLowerCase() + "_" + collect;
            }
        }
        List<String> objects = ListTs.newArrayList();
        objects.add("create");
        if (StrUtil.isNotBlank(indexTypeName)) {
            objects.add(indexTypeName);
        }
        objects.add("index");
        objects.add(name);
        objects.add("on");
        String schema = StrUtil.blankToDefault(ddlIndexInfo.getSchema(), "");
        String tableName = ddlIndexInfo.getTableName();
        String join = (StrUtil.isBlank(schema) ? "" : schema + SP.DOT) + tableName;
        objects.add(join);
        objects.add("(");
        String nameA = ListTs.asList(keys)
                .stream()
                .map(StrUtil::toUnderlineCase).collect(Collectors.joining(","));
        objects.add(nameA);
        objects.add(")");
        return String.join(SP.SPACE, objects);
    }
}
