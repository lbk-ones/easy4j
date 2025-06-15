package template.service.controller;

import easy4j.infra.common.header.EasyResult;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import template.service.domains.Account;
import template.service.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/account-apply")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // 新增账户
    @PostMapping
    public EasyResult<Object> addAccount(@RequestBody Account account) {
        boolean success = accountService.save(account);
        return EasyResult.ok(success);
    }

    // 根据ID删除账户
    @DeleteMapping("/{patId}")
    public EasyResult<Object> deleteAccount(@PathVariable String patId) {
        boolean success = accountService.removeById(patId);
        return EasyResult.ok(success);
    }

    // 更新账户信息
    @PutMapping
    public EasyResult<Object> updateAccount(@RequestBody Account account) {
        boolean success = accountService.updateById(account);
        return EasyResult.ok(success);
    }

    // 根据ID查询账户
    @GetMapping("/{patId}")
    public EasyResult<Account> getAccount(@PathVariable String patId) {
        Account account = accountService.getById(patId);
        return EasyResult.ok(account);
    }

    // 查询所有账户
    @GetMapping("/list")
    public EasyResult<Object> listAccounts() {
        List<Account> list = accountService.list();
        return EasyResult.ok(list);
    }

    // 分页查询账户
    @GetMapping("/page")
    public EasyResult<Object> pageAccounts(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Account> page = new Page<>(pageNum, pageSize);
        Page<Account> resultPage = accountService.page(page);
        return EasyResult.ok(resultPage);
    }

    // 根据余额范围查询
    @GetMapping("/search")
    public EasyResult<Object> searchByBalance(@RequestParam Integer minBalance,
                                  @RequestParam Integer maxBalance) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("BALANCE", minBalance, maxBalance);
        List<Account> list = accountService.list(queryWrapper);
        return EasyResult.ok(list);
    }
}