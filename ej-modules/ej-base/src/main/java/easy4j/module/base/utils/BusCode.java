package easy4j.module.base.utils;

import easy4j.module.base.annotations.Desc;

/**
     A00001=操作成功
     A00002=操作失败
     A00003=系统错误
     A00004=参数{0}不能为空
     A00005=HTTP请求参数异常,{0}
     A00006=接口不接受当前的ContentType请求
     A00007=不允许使用此HTTP方法请求接口数据
 */
public class BusCode {

    @Desc("操作成功")
    public static final String A00001 = "A00001";
    @Desc("操作失败")
    public static final String A00002 = "A00002";
    @Desc("系统错误")
    public static final String A00003 = "A00003";
    @Desc("带错误信息的系统错误")
    public static final String A000031 = "A000031";
    @Desc("参数不能为空")
    public static final String A00004 = "A00004";
    @Desc("HTTP请求参数异常")
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
}
