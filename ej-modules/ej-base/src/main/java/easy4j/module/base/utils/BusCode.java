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


}
