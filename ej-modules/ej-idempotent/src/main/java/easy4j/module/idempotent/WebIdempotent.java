package easy4j.module.idempotent;

import easy4j.module.base.annotations.Desc;

import java.lang.annotation.*;

/**
 * WebIdempotent
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebIdempotent {

    // 唯一key的获取方式
    @Desc("唯一key的获取方式 take 代表从前端获取 默认值为 XIdempotentKey")
    String keyGeneratorType() default "take";

    @Desc("存储方式 默认db 代表把唯一key存储到数据库中去")
    StorageTypeEnum storageType() default StorageTypeEnum.DB;

    @Desc("key的过期时间")
    int expireSeconds() default 60*5;

}