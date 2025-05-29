package easy4j.module.idempotent;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.plugin.idempotent.Easy4jIdempotentKeyGenerator;
import easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IdempotentToolFactory
 *
 * @author bokun.li
 * @date 2025-05
 */
@Component
public class IdempotentToolFactory {
    private static final AtomicInteger hasRedis = new AtomicInteger(0);

    @Autowired
    private Map<String, Easy4jIdempotentKeyGenerator> keyGenerators;

    @Autowired
    private Map<String, Easy4jIdempotentStorage> storageServices;

    public Easy4jIdempotentKeyGenerator getKeyGenerator(String type) {
        return keyGenerators.get(type + "KeyGenerator");
    }

    public Easy4jIdempotentStorage getStorage(StorageTypeEnum type) throws RuntimeException {
        if (type == StorageTypeEnum.REDIS) {
            try {
                if (hasRedis.get() == 0) {
                    hasRedis.set(1);
                    this.getClass().getClassLoader().loadClass("easy4j.module.redis.Config");
                }
            } catch (Exception e) {
                throw new RuntimeException("未引入redis模块请 请引入 ej-redis 模块");
            }
        }
        return storageServices.get(type.getType() + "IdempotentStorage");
    }
}