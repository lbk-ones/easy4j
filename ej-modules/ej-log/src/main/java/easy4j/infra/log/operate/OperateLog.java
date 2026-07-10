package easy4j.infra.log.operate;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 在方法上标注此注解，自动记录操作日志
 *
 * 使用示例：
 * @OperationLog(module = "user", action = "create", description = "创建用户")
 * public User createUser(@RequestBody User user) {
 *     // 业务逻辑
 * }
 *
 * @author easy4j
 * @date 2026/7/10
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 操作模块（如：user, order, system）
     * 必填项
     */
    String module();

    /**
     * 操作动作（如：create, update, delete, login, export）
     * 必填项
     */
    String action();

    /**
     * 操作描述（如：创建用户、修改订单状态）
     * 可选项
     */
    String description() default "";

    /**
     * 业务类型
     * 可选项
     */
    String businessType() default "";

    /**
     * 业务流水号
     * 可选项，如不指定则自动生成UUID
     */
    String businessNo() default "";

    /**
     * 业务主键取值表达式
     * 支持 SpEL 表达式或简单的参数名称
     * <p>
     * 示例：
     * - "orderId" — 直接取参数 orderId
     * - "#order.id" — 取 order 对象的 id 属性
     * - "#order.id + '-' + #order.status" — 表达式组合
     * - "#getBusinessId(#order)" — 调用自定义方法
     * <p>
     * 支持的变量：
     * - #参数名 — 对应方法参数
     * - #result — 返回结果（仅在后处理时可用）
     */
    String businessIdExpression() default "";

    /**
     * 是否记录请求参数
     * 默认false
     */
    boolean recordParams() default false;

    /**
     * 是否记录响应数据
     * 默认false
     */
    boolean recordResult() default false;

}
