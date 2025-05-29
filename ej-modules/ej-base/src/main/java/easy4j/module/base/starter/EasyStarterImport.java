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
package easy4j.module.base.starter;

import easy4j.module.base.exception.GlobalExceptionHandler;
import easy4j.module.base.web.WebFilterConfig;
import easy4j.module.base.web.WebMvcConvertConfig;
import easy4j.module.base.web.WebMvcCorsConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 引入 bean
 *
 * @author bokun.li
 * @date 2023/11/19
 */
public class EasyStarterImport implements InitializingBean, ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        return new String[]{
                ApplicationRuner.class.getName(),
                Easy4j.class.getName(),
                GlobalExceptionHandler.class.getName(),
                WebMvcCorsConfig.class.getName(),
                WebMvcConvertConfig.class.getName(),
                WebFilterConfig.class.getName()
        };
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
