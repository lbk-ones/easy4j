package template.service.order.controller;


import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.EasyResult;
import easy4j.module.sauth.annotations.NoLogin;
import easy4j.module.seed.CommonKey;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import template.service.order.domains.AdviceOrder;
import template.service.order.service.AdviceOrderService;
import template.service.order.service.tcc.AccountTccAction;
import template.service.order.service.tcc.StorageTccAction;

import javax.validation.Valid;

/**
 * OrderTccController
 * tcc测试
 *
 * @author bokun.li
 * @date 2025/6/27
 */
@RestController
@RequestMapping("/tcc-advice-order")
public class OrderTccController {

    @Autowired
    AccountTccAction accountTccAction;

    @Autowired
    StorageTccAction storageTccAction;


    @Autowired
    AdviceOrderService adviceOrderService;

    @NoLogin
    @PostMapping("/tcc-create")
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(name = "tcc-localTccCreate")
    public EasyResult<Object> localTccCreate(@Valid @RequestBody AdviceOrder adviceOrder) {

        try {
            Easy4j.info("localTccCreate--->,{}" + Thread.currentThread().getName());
            // 划帐
            accountTccAction.prepare(null, adviceOrder);

            // 减库存
            storageTccAction.prepare(null, adviceOrder);

            // 生成订单信息
            adviceOrder.setOrderNo(CommonKey.gennerString());
            adviceOrderService.save(adviceOrder);
        } catch (Exception e) {
            Easy4j.error("error-localTccCreate--->,{},{}" + Thread.currentThread().getName(), e.getMessage());
            throw e;
        }
        return EasyResult.ok(null);
    }


}
