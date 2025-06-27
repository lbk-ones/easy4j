package template.service.order.service.tcc.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.sca.seata.BaseTccAction;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import template.service.api.client.TemplateAccountApi;
import template.service.api.client.TemplateStorageApi;
import template.service.api.dto.AccountDto;
import template.service.api.dto.AdviceStorageDto;
import template.service.order.domains.AdviceOrder;
import template.service.order.service.tcc.AccountTccAction;

import java.util.Map;


@LocalTCC
@Service
@Slf4j
public class AccountTccActionActionImpl extends BaseTccAction implements AccountTccAction {

    @Autowired
    TemplateAccountApi templateAccountApi;

    @Autowired
    private TemplateStorageApi templateStorageApi;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @TwoPhaseBusinessAction(name = "tcc-account-action", commitMethod = "commit", rollbackMethod = "cancel", useTCCFence = true)
    public EasyResult<Object> prepare(BusinessActionContext context, AdviceOrder adviceOrder) {
        Easy4j.info("tcc-account-action--->,{}" + Thread.currentThread().getName());
        String actionType = "prepare";
        logTx(context, actionType);
        // 查询项目单价
        String patId = adviceOrder.getPatId();
        int tPrice = getPrice(adviceOrder, true);
        // 冻结余额
        AccountDto accountDto = new AccountDto();
        accountDto.setPatId(patId);
        accountDto.setFrozeAmount(tPrice);
        EasyResult<Object> objectEasyResult = templateAccountApi.tccFreeze(accountDto);
        CheckUtils.checkRpcData(objectEasyResult);

        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("advice", adviceOrder);
        context.setActionContext(paramMap);
        return objectEasyResult;
    }


    private int getPrice(AdviceOrder adviceOrder, boolean checkBalance) {
        CheckUtils.checkByLambda(adviceOrder,
                AdviceOrder::getPatId,
                AdviceOrder::getNum,
                AdviceOrder::getOrdCode
        );
        String patId = adviceOrder.getPatId();
        String ordCode1 = adviceOrder.getOrdCode();
        EasyResult<AdviceStorageDto> storage = templateStorageApi.getStorage(ordCode1);
        CheckUtils.checkRpcData(storage);
        AdviceStorageDto data = storage.getData();
        Integer price = data.getPrice();
        Integer num = adviceOrder.getNum();
        int tPrice = price * num;
        if (checkBalance) {
            // 查询账户余额
            EasyResult<AccountDto> account = templateAccountApi.getAccount(patId);
            CheckUtils.checkRpcData(account);
            AccountDto data1 = account.getData();
            int balance = ObjectUtil.defaultIfNull(data1.getBalance(), 0) - ObjectUtil.defaultIfNull(data1.getFrozeAmount(), 0);
            CheckUtils.checkTrue(tPrice > balance, "B00007");
        }


        return tPrice;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EasyResult<Object> commit(BusinessActionContext context) {
        Object advice = context.getActionContext("advice");
        if (null == advice) {
            return null;
        }
        logTx(context, "commit");
        AdviceOrder advice1 = (AdviceOrder) advice;
        AccountDto accountDto = new AccountDto();
        accountDto.setPatId(advice1.getPatId());
        int i = getPrice(advice1, true);
        accountDto.setReduceAmount(i);
        EasyResult<Object> objectEasyResult = templateAccountApi.tccReduce(accountDto);
        CheckUtils.checkRpcRes(objectEasyResult);
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EasyResult<Object> cancel(BusinessActionContext context) {
        Object advice = context.getActionContext("advice");
        if (null == advice) {
            return null;
        }
        logTx(context, "cancel");
        AdviceOrder advice1 = (AdviceOrder) advice;
        AccountDto accountDto = new AccountDto();
        accountDto.setPatId(advice1.getPatId());
        accountDto.setUnFrozeAmount(getPrice(advice1, false));
        EasyResult<Object> objectEasyResult = templateAccountApi.tccUnFreeze(accountDto);
        CheckUtils.checkRpcRes(objectEasyResult);
        return null;
    }
}
