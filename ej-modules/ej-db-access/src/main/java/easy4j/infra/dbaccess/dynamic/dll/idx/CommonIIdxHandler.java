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

    /**
     * 过滤特殊符号，然后转下划线
     *
     * @param wt
     * @return
     */
    public String replaceSpecialSymbol(String wt) {
        if (StrUtil.isBlank(wt)) return "";
        return wt.replaceAll("[^a-zA-Z0-9_\u4e00-\u9fa5]", "_").replaceAll("_+", "_");
    }

    @Override
    public String getIdx(DDLIndexInfo ddlIndexInfo) {
        String name = ddlIndexInfo.getName();
        String[] keys = ddlIndexInfo.getKeys();
        CheckUtils.checkTrue(keys == null || keys.length == 0, "keys not allow be empty");
        String indexTypeName = StrUtil.blankToDefault(ddlIndexInfo.getIndexTypeName(), "");
        if (StrUtil.isBlank(name)) {
            if (keys != null && keys.length > 0) {
                String lowerCase = StrUtil.blankToDefault(indexTypeName.toLowerCase(), ddlIndexInfo.getIndexNamePrefix());
                String collect = ListTs.asList(keys)
                        .stream()
                        .map(StrUtil::toUnderlineCase)
                        .collect(Collectors.joining("_"));

                collect = replaceSpecialSymbol(collect);
                name = (StrUtil.isBlank(lowerCase) ? "" : (lowerCase + "_")) + "idx_" + ddlIndexInfo.getTableName().toLowerCase() + "_" + collect;
                name = replaceSpecialSymbol(name);
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
