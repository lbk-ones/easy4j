package template.service.api.client;

import easy4j.infra.common.header.EasyResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import template.service.api.dto.AccountDto;

@FeignClient(name = "template-account")
public interface TemplateAccountApi {

    // 获取账户，获取账户信息
    @GetMapping("/account-apply/{patId}")
    EasyResult<AccountDto> getAccount(@PathVariable String patId);

    // 修改余额
    @PutMapping("/account-apply")
    EasyResult<Object> updateAccount(@RequestBody AccountDto account);


    // 金额冻结
    @PutMapping("/account-apply/tcc-freeze")
    EasyResult<Object> tccFreeze(@RequestBody AccountDto account);


    // 解冻
    @PutMapping("/account-apply/tcc-unfreeze")
    EasyResult<Object> tccUnFreeze(@RequestBody AccountDto account);


    // 划扣
    @PutMapping("/account-apply/tcc-reduce")
    EasyResult<Object> tccReduce(@RequestBody AccountDto account);


}
