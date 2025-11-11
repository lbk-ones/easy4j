package template.service.order.service.tcc;

import easy4j.infra.common.header.EasyResult;
import org.apache.seata.rm.tcc.api.BusinessActionContext;
import template.service.order.domains.AdviceOrder;


public interface StorageTccAction {


    EasyResult<Object> prepare(BusinessActionContext context, AdviceOrder adviceOrder);


    EasyResult<Object> commit(BusinessActionContext context);


    EasyResult<Object> cancel(BusinessActionContext context);

}
