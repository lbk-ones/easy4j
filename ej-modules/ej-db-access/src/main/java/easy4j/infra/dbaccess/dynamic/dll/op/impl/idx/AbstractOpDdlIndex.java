package easy4j.infra.dbaccess.dynamic.dll.op.impl.idx;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpDdlIndex;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractOpDdlIndex implements OpDdlIndex {

    OpContext opContext;


    public static final String INDEX_TYPE_NAME = "INDEX_TYPE_NAME";
    public static final String INDEX_NAME = "INDEX_NAME";
    public static final String TABLE_NAME = "TABLE_NAME";
    public static final String USING = "USING";
    public static final String COLUMNS = "COLUMNS";

    // 保存参数名称的map
    private static final Map<String, String> FIELD_MAP = Maps.newHashMap();
    // 额外的参数 这个会覆盖 getTemplateParams 方法获取的参数
    private final Map<String, String> extParamMap = Maps.newHashMap();

    static {
        FIELD_MAP.put(INDEX_TYPE_NAME, INDEX_TYPE_NAME);
        FIELD_MAP.put(INDEX_NAME, INDEX_NAME);
        FIELD_MAP.put(TABLE_NAME, TABLE_NAME);
        FIELD_MAP.put(USING, USING);
        FIELD_MAP.put(COLUMNS, COLUMNS);
    }

    @Override
    public void setOpContext(OpContext opContext) {
        if (this.opContext == null) {
            this.opContext = opContext;
        }
    }

    public String getTemplate(){
        return "create [INDEX_TYPE_NAME] index [INDEX_NAME] ON [USING] [TABLE_NAME] ([COLUMNS])";
    }

    /**
     * 如果模板参数不是默认的那些参数名称，那么子类就要调用这个方法给模板参数传值
     *
     * @author bokun.li
     * @date 2025-08-24
     */
    public void put(String field, String value) {
        FIELD_MAP.putIfAbsent(field, field);
        extParamMap.putIfAbsent(field, value);
    }

    public Map<String, String> getTemplateParams(DDLIndexInfo ddlIndexInfo) {
        Map<@Nullable String, @Nullable String> res = Maps.newHashMap();
        OpConfig opConfig = this.getOpContext().getOpConfig();
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

                collect = opConfig.replaceSpecialSymbol(collect);
                name = (StrUtil.isBlank(lowerCase) ? "" : (lowerCase + "_")) + "idx_" + ddlIndexInfo.getTableName().toLowerCase() + "_" + collect;
                name = opConfig.replaceSpecialSymbol(name);
            }
        }
        if (StrUtil.isNotBlank(indexTypeName)) {
            res.put(INDEX_TYPE_NAME,indexTypeName);
        }

        res.put(INDEX_NAME,name);

        String schema = StrUtil.blankToDefault(ddlIndexInfo.getSchema(), "");
        String tableName = ddlIndexInfo.getTableName();
        String join = (StrUtil.isBlank(schema) ? "" : schema + SP.DOT) + tableName;
        res.put(TABLE_NAME,join);


        String using = ddlIndexInfo.getUsing();
        if (StrUtil.isNotBlank(using)) {
            res.put(USING,"using " + using);
        }

        String nameA = ListTs.asList(keys)
                .stream()
                .map(StrUtil::toUnderlineCase).collect(Collectors.joining(","));
        res.put(COLUMNS,nameA);
        return res;
    }



    @Override
    public String getIndexes(DDLIndexInfo ddlFieldInfo) {
        CheckUtils.notNull(ddlFieldInfo,"ddlFieldInfo");
        CheckUtils.checkByLambda(ddlFieldInfo,DDLIndexInfo::getTableName);
        OpConfig opConfig = this.getOpContext().getOpConfig();
        return opConfig.patchStrWithTemplate(ddlFieldInfo, getTemplate(), FIELD_MAP, extParamMap, this::getTemplateParams);
    }
}
