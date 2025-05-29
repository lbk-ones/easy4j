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
package easy4j.module.sauth.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 将角色和权限展开
 * 角色：字符串
 * 权限：字符串
 */
@Data
public class SecurityAuthority implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 权限代码
     */
    private String authorityCode;


    /**
     * 权限名称
     */
    private String authorityName;


    /**
     * 请求地址 ant风格 /api/**
     */
    private String requestUri;

    /**
     * 额外信息
     */
    private Map<String, Object> extMap;

}
