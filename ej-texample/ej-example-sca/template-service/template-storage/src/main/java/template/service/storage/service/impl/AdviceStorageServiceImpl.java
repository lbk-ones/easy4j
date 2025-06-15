package template.service.storage.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import template.service.storage.domains.AdviceStorage;
import template.service.storage.mapper.AdviceStorageMapper;
import template.service.storage.service.AdviceStorageService;

@Service
public class AdviceStorageServiceImpl extends ServiceImpl<AdviceStorageMapper, AdviceStorage> implements AdviceStorageService {

    @Override
    @Transactional
    public boolean decreaseCount(String ordCode, int quantity) {
        // 库存扣减（带乐观锁）
        UpdateWrapper<AdviceStorage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("ORD_CODE", ordCode)
                     .ge("COUNT", quantity); // 确保库存充足
        
        AdviceStorage storage = new AdviceStorage();
        storage.setOrdCode(ordCode);
        storage.setCount(-quantity); // 负数表示扣减
        
        return update(updateWrapper.setSql("COUNT = COUNT - " + quantity));
    }
}