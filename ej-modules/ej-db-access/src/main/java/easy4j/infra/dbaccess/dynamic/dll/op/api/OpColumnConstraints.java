package easy4j.infra.dbaccess.dynamic.dll.op.api;

import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;

/**
 * OpColumnConstraints
 * 列约束
 *
 * @author bokun.li
 * @date 2025/8/23
 */
public interface OpColumnConstraints extends IOpContext {

    boolean match(OpContext opContext);


}
