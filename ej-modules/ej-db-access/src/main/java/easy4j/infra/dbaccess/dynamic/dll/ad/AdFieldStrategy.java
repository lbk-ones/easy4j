package easy4j.infra.dbaccess.dynamic.dll.ad;

import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
/**
 * AdFieldStrategy
 *
 * @author bokun.li
 * @date 2025/8/20
 */
public interface AdFieldStrategy {

    String getColumnSegment(DDLConfig ddlConfig);
    String getColumnComment(DDLConfig ddlConfig);
}
