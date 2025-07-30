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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 将角色和权限展开
 * 角色：字符串
 * 权限：字符串
 */
@Data
@Schema(description = "通用权限")
public class SecurityAuthority implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 角色代码
     */
    @Schema(description = "角色代码")
    private String roleCode;

    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 权限代码
     */
    @Schema(description = "权限代码")
    private String authorityCode;


    /**
     * 权限名称
     */
    @Schema(description = "权限名称")
    private String authorityName;


    /**
     * 菜单code
     */
    @Schema(description = "菜单code")
    private String menuCode;


    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String menuName;


    /**
     * 请求地址 ant风格 /api/**
     */
    @Schema(description = "请求地址 ant风格 /api/**")
    private String requestUri;

    /**
     * 是否有效
     */
    @Schema(description = "是否有效")
    private boolean isEnabled;

    /**
     * 权限类别 菜单、应用、列表、按钮等
     */
    @Schema(description = "权限类别 菜单、应用、列表、按钮等")
    private String authorityType;


    /**
     * 权限组
     */
    @Schema(description = "权限组")
    private String group;

    /**
     * 额外信息
     */
    @Schema(description = "额外信息")
    private Map<String, Object> extMap;

}
