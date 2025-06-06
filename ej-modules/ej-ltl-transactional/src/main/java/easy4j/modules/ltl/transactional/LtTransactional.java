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
package easy4j.modules.ltl.transactional;


import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * LtTransactional
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Transactional
public @interface LtTransactional {


    @AliasFor(
            annotation = Transactional.class,
            attribute = "rollbackFor"
    )
    Class<? extends Throwable>[] rollbackFor() default {};

    @AliasFor(
            annotation = Transactional.class,
            attribute = "propagation"
    )
    Propagation propagation() default Propagation.REQUIRED;


    @AliasFor(
            annotation = Transactional.class,
            attribute = "isolation"
    )
    Isolation isolation() default Isolation.DEFAULT;


    String businessKey() default "";
    String businessName() default "";

    String baenName() default "";
    String beanMethod() default "";
    int retryCount() default 3;


}
