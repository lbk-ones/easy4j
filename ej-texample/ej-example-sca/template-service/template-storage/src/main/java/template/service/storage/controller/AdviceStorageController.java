package template.service.storage.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.BusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import template.service.api.dto.AdviceStorageDto;
import template.service.storage.domains.AdviceStorage;
import template.service.storage.service.AdviceStorageService;

import java.util.List;

@RestController
@RequestMapping("advice-storage")
public class AdviceStorageController {

    @Autowired
    private AdviceStorageService storageService;

    // 新增库存记录
    @PostMapping
    public EasyResult<Object> addStorage(@RequestBody AdviceStorage storage) {
        boolean success = storageService.save(storage);
        return EasyResult.ok(success);
    }

    // 根据项目代码删除库存记录
    @DeleteMapping("/{ordCode}")
    public EasyResult<Object> deleteStorage(@PathVariable String ordCode) {
        boolean success = storageService.removeById(ordCode);
        return EasyResult.ok(success);
    }

    // 更新库存记录
    @PutMapping
    public EasyResult<Object> updateStorage(@RequestBody AdviceStorage storage) {
        boolean success = storageService.updateById(storage);
        return EasyResult.ok(success);
    }

    // 根据项目代码查询库存
    @GetMapping("/{ordCode}")
    public EasyResult<Object> getStorage(@PathVariable String ordCode) {
        AdviceStorage storage = storageService.getById(ordCode);
        return EasyResult.ok(storage);
    }

    // 查询所有库存记录
    @GetMapping("/list")
    public EasyResult<Object> listStorages() {
        List<AdviceStorage> list = storageService.list();
        return EasyResult.ok(list);
    }

    // 分页查询库存记录
    @GetMapping("/page")
    public EasyResult<Object> pageStorages(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AdviceStorage> page = new Page<>(pageNum, pageSize);
        Page<AdviceStorage> resultPage = storageService.page(page);
        return EasyResult.ok(resultPage);
    }

    // 查询库存大于指定数量的项目
    @GetMapping("/search")
    public EasyResult<Object> searchByCount(@RequestParam Integer minCount) {
        QueryWrapper<AdviceStorage> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("COUNT", minCount);
        List<AdviceStorage> list = storageService.list(queryWrapper);
        return EasyResult.ok(list);
    }

    // 扣减库存
    @PostMapping("/decrease/{ordCode}/{quantity}")
    public EasyResult<Object> decreaseStorage(@PathVariable String ordCode, @PathVariable int quantity) {
        boolean success = storageService.decreaseCount(ordCode, quantity);
        return EasyResult.ok(success);
    }


    @PutMapping("/advice-tccFrozeStorage")
    EasyResult<AdviceStorageDto> tccFrozeStorage(@RequestBody AdviceStorageDto storage) {
        CheckUtils.checkByLambda(storage,
                AdviceStorageDto::getOrdCode,
                AdviceStorageDto::getCount
        );
        String ordCode = storage.getOrdCode();
        AdviceStorage byId = storageService.getById(ordCode);
        CheckUtils.checkObjIsNull(byId, BusCode.A00046, ordCode, "storage");
        Integer dbFrozeAmount = ObjectUtil.defaultIfNull(byId.getFrozeAmount(), 0);
        // 不能超过库存
        int min = Math.min(byId.getCount(), storage.getFrozeAmount() + dbFrozeAmount);
        byId.setFrozeAmount(min);
        storageService.updateById(byId);
        return EasyResult.ok(null);
    }

    @PutMapping("/advice-tccReduceStorage")
    EasyResult<AdviceStorageDto> tccReduceStorage(@RequestBody AdviceStorageDto storage) {
        CheckUtils.checkByLambda(storage,
                AdviceStorageDto::getOrdCode,
                AdviceStorageDto::getCount
        );

        String ordCode = storage.getOrdCode();
        Integer frozeAmount1 = storage.getFrozeAmount();
        AdviceStorage byId = storageService.getById(ordCode);
        CheckUtils.checkObjIsNull(byId, BusCode.A00046, ordCode, "storage");
        Integer count = byId.getCount();
        Integer frozeAmount = ObjectUtil.defaultIfNull(byId.getFrozeAmount(), 0);
        // 不能减到负数
        int min = Math.max(0, frozeAmount - frozeAmount1);
        byId.setFrozeAmount(min);
        byId.setCount(Math.max(0, count - frozeAmount1));
        storageService.updateById(byId);
        return EasyResult.ok(null);
    }

    // 取消冻结 将之前冻结的数量减回去
    @PutMapping("/advice-tccCancelStorage")
    EasyResult<AdviceStorageDto> tccCancelStorage(@RequestBody AdviceStorageDto storage) {
        CheckUtils.checkByLambda(storage,
                AdviceStorageDto::getOrdCode,
                AdviceStorageDto::getCount
        );
        String ordCode = storage.getOrdCode();
        Integer frozeAmount1 = storage.getFrozeAmount();
        AdviceStorage byId = storageService.getById(ordCode);
        CheckUtils.checkObjIsNull(byId, BusCode.A00046, ordCode, "storage");
        Integer dbFrozeAmount = ObjectUtil.defaultIfNull(byId.getFrozeAmount(), 0);
        // 不能减到负数
        byId.setFrozeAmount(Math.max(0, dbFrozeAmount - frozeAmount1));
        storageService.updateById(byId);
        return EasyResult.ok(null);
    }
}