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
    @Schema(description = "菜单code不能以数字开头")
    private String menuCode;

    /**
     * 上级菜单code
     */
    @Schema(description = "上级菜单code")
    private String parentMenuCode;


    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    private String menuName;


    /**
     * 路由/请求地址
     */
    @Schema(description = "菜单路由/外部请求地址")
    private String path;

    /**
     * 排序号
     */
    @Schema(description = "排序号")
    private Integer order;

    /**
     * 图标
     */
    @Schema(description = "图标")
    private String icon;

    /**
     * 前端i18n码
     */
    @Schema(description = "前端i18n码")
    private String locale;


    /**
     * 是否有效
     */
    @Schema(description = "是否有效")
    private boolean isEnabled;

    /**
     * 权限类别 1菜单 2资源 3接口
     */
    @Schema(description = "权限类别 1菜单 2资源 3接口")
    private Integer authorityType;


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
