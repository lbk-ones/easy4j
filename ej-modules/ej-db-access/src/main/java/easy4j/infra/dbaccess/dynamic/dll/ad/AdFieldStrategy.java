package easy4j.infra.dbaccess.dynamic.dll.ad;

import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;

/**
 * AdFieldStrategy
 *
 * @author bokun.li
 * @date 2025/8/20
 * @see DynamicDDL
 */
@Deprecated
public interface AdFieldStrategy {

    String getColumnSegment(DDLConfig ddlConfig);
    String getColumnComment(DDLConfig ddlConfig);
}
