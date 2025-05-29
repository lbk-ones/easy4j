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
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaBaseGen
 *
 * @author bokun.li
 * @date 2025-05
 */
@Setter
@Getter
public class JavaBaseGen {

    @Desc("当前包的名称")
    private String currentPackageName;

    @Desc("当前模块实体名称")
    private String domainName;

    @Desc("当前模块实体名称第一个字母小写")
    private String firstLowDomainName;

    @Desc("引入的包有哪些不需要加入 import 会自动加入")
    public List<String> importList = new ArrayList<>();

    @Desc("类上需要加的注解")
    public List<String> annotationList = new ArrayList<>();

    @Desc("类上需要加的注释")
    public List<String> lineList = new ArrayList<>();

    @Desc("方法")
    public List<JavaBaseMethod> methodList = new ArrayList<>();
    public List<JavaBaseMethod> fieldList = new ArrayList<>();


}
