package template.service.order.service.tcc.impl;

import cn.hutool.core.util.ObjectUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.sca.seata.BaseTccAction;
import org.apache.seata.rm.tcc.api.BusinessActionContext;
import org.apache.seata.rm.tcc.api.BusinessActionContextParameter;
import org.apache.seata.rm.tcc.api.LocalTCC;
import org.apache.seata.rm.tcc.api.TwoPhaseBusinessAction;
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


@LocalTCC
@Service
@Slf4j
public class AccountTccActionActionImpl extends BaseTccAction implements AccountTccAction {

    @Autowired
    TemplateAccountApi templateAccountApi;

    @Autowired
    private TemplateStorageApi templateStorageApi;

    /**
     * 就算seata没有 那也要正常的运行才对
     *
     * @author bokun.li
     * @date 2025/6/30
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @TwoPhaseBusinessAction(name = "tcc-account-action", commitMethod = "commit", rollbackMethod = "cancel", useTCCFence = true)
    public EasyResult<Object> prepare(BusinessActionContext _context, @BusinessActionContextParameter("advice") AdviceOrder adviceOrder) {

        // 这个是为了兼容没有seata的情况
        BusinessActionContext context = getOrCreateContext(_context, e -> {
            putContext(e, "advice", adviceOrder);
            return e;
        });
        // 这个也是为了兼容没有seata的情况
        return prepareCallback(() -> {
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

            putContext(context, "cs", "2312561");
            return objectEasyResult;
        }, () -> this.commit(context));
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


    @Transactional(rollbackFor = Exception.class)
    public void commit(BusinessActionContext context) {
        AdviceOrder advice1 = context.getActionContext("advice", AdviceOrder.class);
        if (null == advice1) {
            return;
        }
        logTx(context, "commit");
        //AdviceOrder advice1 = JacksonUtil.toObject(JacksonUtil.toJson(advice), AdviceOrder.class);
        //AdviceOrder advice1 = (AdviceOrder) advice;
        AccountDto accountDto = new AccountDto();
        accountDto.setPatId(advice1.getPatId());
        int i = getPrice(advice1, true);
        accountDto.setReduceAmount(i);
        EasyResult<Object> objectEasyResult = templateAccountApi.tccReduce(accountDto);
        CheckUtils.checkRpcRes(objectEasyResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(BusinessActionContext context) {
        AdviceOrder advice1 = context.getActionContext("advice", AdviceOrder.class);
        if (null == advice1) {
            return;
        }
        logTx(context, "cancel");
        AccountDto accountDto = new AccountDto();
        accountDto.setPatId(advice1.getPatId());
        accountDto.setUnFrozeAmount(getPrice(advice1, false));
        EasyResult<Object> objectEasyResult = templateAccountApi.tccUnFreeze(accountDto);
        CheckUtils.checkRpcRes(objectEasyResult);
    }
}
