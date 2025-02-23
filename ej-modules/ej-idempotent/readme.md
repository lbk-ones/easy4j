

# 介绍
    easy4j-idempotent 主要是对web接口幂等的封装，方便使用，是否幂等由key值决定
# 功能：
    -  使用@WebIdempotent注解即可启用幂等功能
    -  参数解释如下↓
    -       keyWay          （key的生成方式）: header、query、form （代表从header中获取、query中获取、form中获取）
    -       keyName         （key的名称）:    header默认为take take为前台拿取(前台可以放到header，query传参)
    -       storageType     （数据存储方式）:默认为db db会自动建立相关表
    -       expireTime      （key的过期时间）:默认为5分钟 5分钟之后key值自动过期
# 扩展
  -  扩展key的拿值方式：新增bean实现接口easy4j.module.base.plugin.idempotent.Easy4jIdempotentKeyGenerator  并自定义bean的名称为 xxx + KeyGenerator
  -  扩展数据存储的方式：新增bean实现接口easy4j.module.base.plugin.idempotent.Easy4jIdempotentStorage       并自定义bean的名称为 xxx + IdempotentStorage

    xxx 就是 keyWay的值