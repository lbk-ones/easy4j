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
package easy4j.module.jpa.gen;

import easy4j.module.base.annotations.Desc;
import easy4j.module.base.plugin.gen.BaseConfigCodeGen;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.Assert;

import java.io.File;


/**
 * ConfigJpaGen
 *
 * @author bokun.li
 * @date 2025-05
 */
@Getter
public class ConfigJpaGen extends BaseConfigCodeGen {

    private ConfigJpaGen(Builder builder){
        super(builder);

        this.mainClassPackage = builder.mainClassPackage;
        this.javaBaseUrl = builder.javaBaseUrl;
        this.genDtoDateToString = builder.genDtoDateToString;
        this.groupControllerModule = builder.groupControllerModule;
        this.springMainClass = builder.springMainClass;
    }

    @Desc("工作路径（通常是启动类所在包）如果 通过springMainClass()方法设置了springMainClass那么这个可以不用设置")
    private String mainClassPackage;
    @Desc("java绝对路径 到 xxx/src/main 这一级")
    private final String javaBaseUrl;
    @Desc("生成Dto的时候是否将Date转为String")
    private final Boolean genDtoDateToString;
    @Desc("是否按模块生成Api接口文档")
    private final Boolean groupControllerModule;

    @Desc("spring模块主类")
    private final Class<?> springMainClass;

    public static class Builder extends BaseConfigCodeGen.Builder<Builder>{

        @Desc("工作路径（通常是启动类所在包）如果 通过springMainClass()方法设置了springMainClass那么这个可以不用设置")
        private String mainClassPackage;
        @Desc("java绝对路径 到 xxx/src/main 这一级")
        private String javaBaseUrl = System.getProperty("user.dir")+ File.separator+"src"+File.separator+"main";

        @Desc("生成Dto的时候是否将Date转为String")
        private Boolean genDtoDateToString = true;
        @Desc("是否按模块生成Api接口文档")
        private Boolean groupControllerModule = true;

        private Class<?> springMainClass;


        
        public Builder setMainClassPackage(String mainClassPackage) {
            this.mainClassPackage = mainClassPackage;
            return this;
        }

        public Builder setJavaBaseUrl(String javaBaseUrl) {
            this.javaBaseUrl = javaBaseUrl;
            return this;
        }

        public Builder setGenDtoDateToString(Boolean genDtoDateToString) {
            this.genDtoDateToString = genDtoDateToString;
            return this;
        }

        public Builder setGroupControllerModule(Boolean groupControllerModule) {
            this.groupControllerModule = groupControllerModule;
            return this;
        }

        public Builder setSpringMainClass(Class<?> springMainClass) {
            Assert.notNull(springMainClass,"springMainClass must not be null");
            this.springMainClass = springMainClass;
            this.mainClassPackage = springMainClass.getPackage().getName();
            return this;
        }

        public ConfigJpaGen build(){
            this.setNoCheck(true);
            return new ConfigJpaGen(this);
        }

        @Override
        public Builder self() {
            return this;
        }
    }



}
