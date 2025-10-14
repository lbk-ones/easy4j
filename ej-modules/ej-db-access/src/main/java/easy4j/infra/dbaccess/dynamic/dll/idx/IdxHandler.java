package easy4j.infra.dbaccess.dynamic.dll.idx;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.List;
import java.util.Map;
/**
 * @see easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL
 */
@Deprecated
public class IdxHandler {

    private static final List<IIdxHandler> idxHandlerMap = ListTs.newLinkedList();

    static {
        idxHandlerMap.add(new CommonIIdxHandler());
    }

    public static void register(IIdxHandler registerHandler) {
        if (!idxHandlerMap.contains(registerHandler)) {
            idxHandlerMap.add(registerHandler);
        }
    }

    private static IIdxHandler selectIdxHandler(DDLIndexInfo ddlIndexInfo) {
        IIdxHandler commonIIdxHandler = new CommonIIdxHandler();
        for (IIdxHandler idxHandler : idxHandlerMap) {
            String name = idxHandler.getName();
            // 通用解析
            if (StrUtil.equals(name, "common")) {
                commonIIdxHandler = idxHandler;
            }
            if (idxHandler.match(ddlIndexInfo)) {
                return idxHandler;
            }
        }
        return commonIIdxHandler;
    }

    /**
     * 根据模型来处理生成创建索引的语句
     *
     * @author bokun.li
     * @date 2025-08-03
     */
    public static String handlerIdx(DDLIndexInfo ddlIndexInfo) {
        CheckUtils.notNull(ddlIndexInfo, "ddlIndexInfo");
        CheckUtils.checkParam(ddlIndexInfo, "tableName");
        CheckUtils.checkParam(ddlIndexInfo, "keys");
        IIdxHandler iIdxHandler = selectIdxHandler(ddlIndexInfo);
        return iIdxHandler.getIdx(ddlIndexInfo);
    }

    /**
     * 根据注解来处理生成创建索引的语句
     *
     * @author bokun.li
     * @date 2025-08-03
     */
    public static String handlerIdx(DDLIndex ddlIndex, String schema, String tableName) {
        CheckUtils.notNull(ddlIndex, "ddlIndex");
        CheckUtils.notNull(tableName, "tableName");
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(ddlIndex);
        DDLIndexInfo ddlIndexInfo = BeanUtil.mapToBean(annotationAttributes, DDLIndexInfo.class, true, CopyOptions.create().ignoreError());
        ddlIndexInfo.setDdlIndex(ddlIndex);
        ddlIndexInfo.setTableName(tableName);
        ddlIndexInfo.setSchema(schema);
        ddlIndexInfo.setIndexTypeName(ddlIndex.type().getIndexName());
        return handlerIdx(ddlIndexInfo);
    }

}
