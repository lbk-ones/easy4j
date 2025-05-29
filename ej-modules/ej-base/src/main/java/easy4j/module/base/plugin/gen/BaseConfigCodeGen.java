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
package easy4j.module.base.plugin.gen;


import easy4j.module.base.annotations.Desc;
import lombok.Getter;

/**
 * BaseConfigCodeGen
 *
 * @author bokun.li
 * @date 2025-05
 */
@Getter
public class BaseConfigCodeGen {
    public BaseConfigCodeGen(Builder<?> builder) {
        this.outAbsoluteUrl = builder.outAbsoluteUrl;
        this.outPackageName = builder.outPackageName;
        this.outFileName = builder.outFileName;
        this.inputAbsoluteUrl = builder.inputAbsoluteUrl;
        this.inputFileName = builder.inputFileName;
        this.scanPackageName = builder.scanPackageName;
        this.tmplClassPath = builder.tmplClassPath;
        this.genNote = builder.genNote;
        this.noCheck = builder.noCheck;
        this.mainSpringClass = builder.mainSpringClass;
    }

    // outAbsoluteUrl + outPackageName + outFileName
    @Desc("输出的绝对路径 不带文件名 不含包的路径")
    private final String outAbsoluteUrl;

    @Desc("输出的包路径")
    private final String outPackageName;

    @Desc("输出的文件名")
    private final String outFileName;

    @Desc("输入的绝对路径")
    private final String inputAbsoluteUrl;

    @Desc("输入的文件名 如果没有那么就针对该包所有的类")
    private final String inputFileName;

    @Desc("扫描的包的全类名")
    private final String scanPackageName;

    @Desc("模板所在路径 相对路径 先从自己所在模块读取")
    private final String tmplClassPath;


    @Desc("是否生成 by easy4j-gen auto generate")
    private final Boolean genNote;


    @Desc("是否生成 by easy4j-gen auto generate")
    private final Class<?> mainSpringClass;

    @Desc("是否跳过检查")
    private final Boolean noCheck;


    public abstract static class Builder<T extends Builder<T>>{

        @Desc("输出的绝对路径 不带文件名 不含包的路径")
        private String outAbsoluteUrl;

        @Desc("输出的包路径")
        private String outPackageName;

        @Desc("输出的文件名")
        private String outFileName;

        @Desc("输入的绝对路径")
        private String inputAbsoluteUrl;

        @Desc("输入的文件名 如果没有那么就针对该包所有的类")
        private String inputFileName;

        @Desc("扫描的包的全类名")
        private String scanPackageName;

        @Desc("模板所在路径 相对路径 先从自己所在模块读取")
        private String tmplClassPath = "tmpl";


        @Desc("是否生成 by easy4j-gen auto generate")
        private Boolean genNote = true;


        @Desc("是否生成 by easy4j-gen auto generate")
        private Class<?> mainSpringClass;


        @Desc("是否跳过检查")
        private Boolean noCheck = false;

        public T setNoCheck(Boolean noCheck) {
            this.noCheck = noCheck;
            return self();
        }
        public T setOutAbsoluteUrl(String outAbsoluteUrl) {
            this.outAbsoluteUrl = outAbsoluteUrl;
            return self();
        }

        public T setOutPackageName(String outPackageName) {
            this.outPackageName = outPackageName;
            return self();
        }

        public T setOutFileName(String outFileName) {
            this.outFileName = outFileName;
            return self();
        }

        public T setInputAbsoluteUrl(String inputAbsoluteUrl) {
            this.inputAbsoluteUrl = inputAbsoluteUrl;
            return self();
        }

        public T setInputFileName(String inputFileName) {
            this.inputFileName = inputFileName;
            return self();
        }

        public T setScanPackageName(String scanPackageName) {
            this.scanPackageName = scanPackageName;
            return self();
        }

        public T setTmplClassPath(String tmplClassPath) {
            this.tmplClassPath = tmplClassPath;
            return self();
        }

        public T setGenNote(Boolean genNote) {
            this.genNote = genNote;
            return self();
        }

        public T setMainSpringClass(Class<?> mainSpringClass) {
            this.mainSpringClass = mainSpringClass;
            return self();
        }
        
        public abstract T self();


    }
}
