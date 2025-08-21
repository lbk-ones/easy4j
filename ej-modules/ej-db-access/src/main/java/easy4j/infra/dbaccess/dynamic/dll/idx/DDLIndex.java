package easy4j.infra.dbaccess.dynamic.dll.idx;

import easy4j.infra.common.annotations.Desc;

import java.lang.annotation.*;

/**
 * DDLIndex
 * 索引注解
 *
 * @author bokun.li
 * @date 2025-08-03
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DDLIndex {

    /**
     * 索引的名称，可以不填，不填会按默认规则生成
     *
     * @return
     */
    String name() default "";

    /**
     * 索引的名称前缀，可以不填
     *
     * @return
     */
    String indexNamePrefix() default "";

    /**
     * 部分数据库索引支持 USING [索引类型]  PostgreSQL、部分MySql、
     */
    @Desc("部分数据库索引支持 USING [索引类型]  PostgreSQL、部分MySql")
    String using() default "";

    /**
     * 索引的键
     * 如果是特殊的键比如千缀索引 就写成这样`name`(20)
     *
     * @return
     */
    String[] keys() default {};

    /**
     * 索引的类型
     *
     * @return
     */
    IndexType type() default IndexType.BTREE;

    /**
     * 特殊索引解析的时候进行参数传参
     *
     * @return
     */
    String[] args() default {};
}
