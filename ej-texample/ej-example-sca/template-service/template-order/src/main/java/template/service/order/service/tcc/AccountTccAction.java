package template.service.order.service.tcc;

import easy4j.infra.common.header.EasyResult;
import io.seata.rm.tcc.api.BusinessActionContext;
import template.service.order.domains.AdviceOrder;

public interface AccountTccAction {


    EasyResult<Object> prepare(BusinessActionContext context, AdviceOrder adviceOrder);


    void commit(BusinessActionContext context);


    void cancel(BusinessActionContext context);

}
