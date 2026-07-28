/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.dbaccess.annotations;

import cn.hutool.db.meta.JdbcType;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.handler.DefaultTypeHandler;
import easy4j.infra.dbaccess.orm.handler.TypeHandler;

import java.lang.annotation.*;

/**
 * JdbcColumn
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JdbcColumn {

    // 列名称
    String name() default "";

    // 列类型
    boolean isPrimaryKey() default false;

    // 主键自动递增
    boolean autoIncrement() default false;

    // 转为json字符串
    boolean toJson() default false;

    // postgresql 特殊类型映射 比如jsonb、json之类的
    @Desc("postgresql 特殊类型映射 比如jsonb、json之类的")
    String pgType() default "";

    // 别称 可以写
    // name as name2
    // TO_CHAR(CREATE_DATE, 'YYYY-MM-DD HH24:MI:SS') AS CREATE_DATE
    String alias() default "";

    // 条件占位符
    String placeHolder() default SP.QUESTION_MARK;

    // jdbcType
    JdbcType jdbcType() default JdbcType.NULL;

    // 类型转换器
    Class<? extends TypeHandler<?>> typeHandler() default DefaultTypeHandler.class;
}
