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
package easy4j.module.base.utils;

import easy4j.module.base.annotations.Desc;

/**
 * A00001=操作成功
 * A00002=操作失败
 * A00003=系统错误
 * A00004=参数{0}不能为空
 * A00005=HTTP请求参数异常,{0}
 * A00006=接口不接受当前的ContentType请求
 * A00007=不允许使用此HTTP方法请求接口数据
 */
public class BusCode {

    @Desc("操作成功")
    public static final String A00001 = "A00001";
    @Desc("操作失败")
    public static final String A00002 = "A00002";
    @Desc("系统错误")
    public static final String A00003 = "A00003";
    @Desc("系统错误 {0}")
    public static final String A000031 = "A000031";
    @Desc("参数{0}不能为空")
    public static final String A00004 = "A00004";
    @Desc("HTTP请求参数异常,{0}")
    public static final String A00005 = "A00005";
    @Desc("接口不接受当前的ContentType请求")
    public static final String A00006 = "A00006";
    @Desc("不允许使用此HTTP方法请求接口数据")
    public static final String A00007 = "A00007";
    @Desc("数据已变更请刷新后再次提交")
    public static final String A00008 = "A00008";
    @Desc("没有可以更新的记录")
    public static final String A00009 = "A00009";
    @Desc("禁用成功")
    public static final String A00010 = "A00010";
    @Desc("启用成功")
    public static final String A00011 = "A00011";
    @Desc("要操作的记录不存在,或者已经被删除")
    public static final String A00012 = "A00012";
    @Desc("要查询的记录不存在")
    public static final String A00013 = "A00013";
    @Desc("dto没有@Id注解")
    public static final String A00014 = "A00014";
    @Desc("主键为空")
    public static final String A00015 = "A00015";
    @Desc("dto没有Version注解")
    public static final String A00016 = "A00016";
    @Desc("版本号version为空")
    public static final String A00017 = "A00017";
    @Desc("版本号version非数字")
    public static final String A00018 = "A00018";
    @Desc("删除失败")
    public static final String A00019 = "A00019";
    @Desc("未找到要删除的数据")
    public static final String A00020 = "A00020";
    @Desc("不能重复请求")
    public static final String A00021 = "A00021";

    @Desc("请求过于频繁，请稍后重试")
    public static final String A00022 = "A00022";

    @Desc("服务暂时不可用，已触发熔断")
    public static final String A00023 = "A00023";

    @Desc("系统降级")
    public static final String A00024 = "A00024";

    @Desc("热点参数限流")
    public static final String A00025 = "A00025";

    @Desc("系统规则限流或降级")
    public static final String A00026 = "A00026";

    @Desc("授权规则不通过")
    public static final String A00027 = "A00027";

    @Desc("未知限流降级")
    public static final String A00028 = "A00028";

    @Desc("缺少请求头{0}")
    public static final String A00029 = "A00029";

    @Desc("请使用POST方式提交")
    public static final String A00030 = "A00030";

    @Desc("用户名不能为空")
    public static final String A00031 = "A00031";
    @Desc("密码不能为空")
    public static final String A00032 = "A00032";

    @Desc("认证失败")
    public static final String A00033 = "A00033";

    @Desc("鉴权失败，非法token")
    public static final String A00034 = "A00034";

    @Desc("会话过期")
    public static final String A00035 = "A00035";

    @Desc("用户检查未通过")
    public static final String A00036 = "A00036";

    @Desc("用户不存在")
    public static final String A00037 = "A00037";

    @Desc("SQL条件不能为空")
    public static final String A00038 = "A00038";

    @Desc("该资源【{0}】已经被锁定，备注：{0}")
    public static final String A00039 = "A00039";

    @Desc("JSON不合法")
    public static final String A00040 = "A00040";

    @Desc("暂时不支持这个功能")
    public static final String A00041 = "A00041";

    @Desc("资源{0}锁定失败，有其他人正在操作")
    public static final String A00042 = "A00042";
}
