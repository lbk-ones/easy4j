package template.service.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.lock.DbLock;
import easy4j.infra.log.RequestLog;
import easy4j.module.idempotent.WebIdempotent;
import easy4j.module.seed.CommonKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import template.service.api.client.TemplateAccountApi;
import template.service.api.client.TemplateStorageApi;
import template.service.api.dto.AccountDto;
import template.service.api.dto.AdviceStorageDto;
import template.service.order.domains.AdviceOrder;
import template.service.order.service.AdviceOrderService;

import java.util.List;
import java.util.Objects;

import static easy4j.infra.common.header.CheckUtils.checkTrue;

@RestController
@RequestMapping("/advice-order")
public class AdviceOrderController {

    @Autowired
    private AdviceOrderService adviceOrderService;

    @Autowired
    private TemplateAccountApi templateAccountApi;

    @Autowired
    private TemplateStorageApi templateStorageApi;


    @Autowired
    Easy4jContext easy4jContext;

    // 新增申请单
    @PostMapping
    @Transactional
    @RequestLog
    @WebIdempotent
    public EasyResult<Object> addAdviceOrder(@RequestBody AdviceOrder adviceOrder) {

        DbLock dbLock = easy4jContext.get(DbLock.class);
        boolean success = false;
        String patId = adviceOrder.getPatId();
        dbLock.lock(patId,30,"订单锁");
        try{
            CheckUtils.checkByLambda(
                    adviceOrder,
                    AdviceOrder::getPatId,
                    AdviceOrder::getOrdCode,
                    AdviceOrder::getNum
            );

            Integer num = adviceOrder.getNum();
            String ordCode = adviceOrder.getOrdCode();
            EasyResult<AccountDto> account = templateAccountApi.getAccount(patId);
            AccountDto data = account.getData();
            checkTrue(num <= 0,"B00003");
            checkTrue(Objects.isNull(data),"B00001");
            Integer balance = data.getBalance();
            checkTrue(balance == null || balance <=0,"B00002",patId);

            EasyResult<AdviceStorageDto> storage = templateStorageApi.getStorage(ordCode);
            checkTrue(!storage.isSuccess(),"B00004",storage.getMessage());

            AdviceStorageDto data1 = storage.getData();
            checkTrue(data1==null,"B00005");
            assert data1 != null;
            Integer price = data1.getPrice();
            checkTrue(price == null || price <=0,"B00006");
            int i = num * price;
            checkTrue(balance<i,"B00007");

            // 减余额
            AccountDto accountDto = new AccountDto();
            accountDto.setPatId(patId);
            accountDto.setBalance(balance-i);
            EasyResult<Object> objectEasyResult = templateAccountApi.updateAccount(accountDto);
            checkTrue(!objectEasyResult.isSuccess(),"B00004",objectEasyResult.getMsgAndError());

            // 减库存
            AdviceStorageDto adviceStorageDto = new AdviceStorageDto();
            adviceStorageDto.setOrdCode(ordCode);
            adviceStorageDto.setCount(data1.getCount()-num);
            EasyResult<Object> objectEasyResult1 = templateStorageApi.updateStorage(adviceStorageDto);
            checkTrue(!objectEasyResult1.isSuccess(),"B00004",objectEasyResult1.getMsgAndError());

            adviceOrder.setOrderNo(CommonKey.gennerString());
            adviceOrderService.save(adviceOrder);
        }   finally {
            dbLock.unLock(patId);
        }
        
        return EasyResult.ok(success);
    }

    // 根据ID删除申请单
    @DeleteMapping("/{orderNo}")
    public EasyResult<Object> deleteAdviceOrder(@PathVariable String orderNo) {
        boolean success = adviceOrderService.removeById(orderNo);
        return EasyResult.ok(success);
    }

    // 更新申请单信息
    @PutMapping
    public EasyResult<Object> updateAdviceOrder(@RequestBody AdviceOrder adviceOrder) {
        boolean success = adviceOrderService.updateById(adviceOrder);
        return  EasyResult.ok(success);
    }

    // 根据ID查询申请单
    @GetMapping("/{orderNo}")
    public EasyResult<Object> getAdviceOrder(@PathVariable String orderNo) {
        AdviceOrder adviceOrder = adviceOrderService.getById(orderNo);
        return EasyResult.ok(adviceOrder);
    }

    // 查询所有申请单
    @GetMapping("/list")
    public EasyResult<Object> listAdviceOrders() {
        List<AdviceOrder> list = adviceOrderService.list();
        return EasyResult.ok(list);
    }

    // 分页查询申请单
    @GetMapping("/page")
    public EasyResult<Object> pageAdviceOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AdviceOrder> page = new Page<>(pageNum, pageSize);
        Page<AdviceOrder> resultPage = adviceOrderService.page(page);
        return EasyResult.ok(resultPage);
    }

    // 根据患者ID查询申请单
    @GetMapping("/search/patient")
    public EasyResult<Object> searchByPatientId(@RequestParam String patId) {
        QueryWrapper<AdviceOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("PAT_ID", patId);
        List<AdviceOrder> list = adviceOrderService.list(queryWrapper);
        return EasyResult.ok(list);
    }

    // 根据项目代码查询申请单
    @GetMapping("/search/code")
    public EasyResult<Object> searchByOrdCode(@RequestParam String ordCode) {
        QueryWrapper<AdviceOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ORD_CODE", ordCode);
        List<AdviceOrder> list = adviceOrderService.list(queryWrapper);
        return EasyResult.ok(list);
    }
}