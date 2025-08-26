package easy4j.infra.dbaccess.dynamic.dll.op.impl.idx;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.idx.CommonIIdxHandler;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

import java.util.List;

/**
 * refactor CommonIIdxHandler
 *
 * @author bokun.li
 * @date 2025/8/26
 * @see CommonIIdxHandler
 */
public class CommonOpDdlIndexImpl extends AbstractOpDdlIndex {


    // 需要额外实现的索引类型
    private final List<String> exclude = ListTs.asList();

    @Override
    public boolean match(OpContext opContext) {
        return !exclude.contains(opContext.getDbType());
    }
}
