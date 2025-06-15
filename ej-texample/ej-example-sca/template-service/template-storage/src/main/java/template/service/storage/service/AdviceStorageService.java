package template.service.storage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import template.service.storage.domains.AdviceStorage;

public interface AdviceStorageService extends IService<AdviceStorage> {
    // 自定义业务方法
    boolean decreaseCount(String ordCode, int quantity);
}
