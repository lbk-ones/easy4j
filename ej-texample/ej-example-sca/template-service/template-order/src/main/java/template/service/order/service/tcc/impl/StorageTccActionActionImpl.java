package template.service.order.service.tcc.impl;

import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.sca.seata.BaseTccAction;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import template.service.api.client.TemplateStorageApi;
import template.service.api.dto.AdviceStorageDto;
import template.service.order.domains.AdviceOrder;
import template.service.order.service.tcc.StorageTccAction;

@LocalTCC
@Service
public class StorageTccActionActionImpl extends BaseTccAction implements StorageTccAction {
    @Autowired
    TemplateStorageApi templateStorageApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TwoPhaseBusinessAction(name = "tcc-storage-action", commitMethod = "commit", rollbackMethod = "cancel", useTCCFence = true)
    public EasyResult<Object> prepare(BusinessActionContext context, @BusinessActionContextParameter("advice") AdviceOrder adviceOrder) {
        return prepareCallback(() -> {
            try {
                String s = "storage-prepare";
                if (lock(context)) {
                    logTx(context, s);
                    String ordCode = adviceOrder.getOrdCode();
                    AdviceStorageDto adviceStorageDto = new AdviceStorageDto();
                    adviceStorageDto.setOrdCode(ordCode);
                    adviceStorageDto.setFrozeAmount(adviceOrder.getNum());
                    EasyResult<AdviceStorageDto> adviceStorageDtoEasyResult = templateStorageApi.tccFrozeStorage(adviceStorageDto);
                    CheckUtils.checkRpcRes(adviceStorageDtoEasyResult);
//                putContext(context, "advice", adviceOrder);
                }

            } finally {
                unLock(context);
            }
            return null;
        }, () -> this.commit(context));
    }

    @Override
    public EasyResult<Object> commit(BusinessActionContext context) {
        logTx(context, "storage-commit");
        AdviceOrder advice2 = context.getActionContext("advice", AdviceOrder.class);
        if (null != advice2) {
            AdviceStorageDto adviceStorageDto = new AdviceStorageDto();
            adviceStorageDto.setOrdCode(advice2.getOrdCode());
            adviceStorageDto.setFrozeAmount(advice2.getNum());
            adviceStorageDto.setCount(advice2.getNum());
            EasyResult<AdviceStorageDto> adviceStorageDtoEasyResult = templateStorageApi.tccReduceStorage(adviceStorageDto);
            CheckUtils.checkRpcRes(adviceStorageDtoEasyResult);
        }
        return null;
    }

    @Override
    public EasyResult<Object> cancel(BusinessActionContext context) {
        logTx(context, "storage-cancel");
        AdviceOrder advice2 = context.getActionContext("advice", AdviceOrder.class);
        if (null != advice2) {
            AdviceStorageDto adviceStorageDto = new AdviceStorageDto();
            adviceStorageDto.setOrdCode(advice2.getOrdCode());
            adviceStorageDto.setFrozeAmount(advice2.getNum());
            adviceStorageDto.setCount(advice2.getNum());
            EasyResult<AdviceStorageDto> adviceStorageDtoEasyResult = templateStorageApi.tccCancelStorage(adviceStorageDto);
            CheckUtils.checkRpcRes(adviceStorageDtoEasyResult);
        }

        return null;
    }
}
