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
package easy4j.sca.account.test.controller;

import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.json.JacksonUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Test
 *
 * @author bokun.li
 * @date 2025-05
 */
@Controller
public class Test {


    @PostMapping("httpGet")
    @ResponseBody
    public EasyResult<String> httpGet() {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        return EasyResult.ok(JacksonUtil.toJson(ejSysProperties));
    }

    @GetMapping("index2")
    String httpGet2() {
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        return "index";
    }
}
