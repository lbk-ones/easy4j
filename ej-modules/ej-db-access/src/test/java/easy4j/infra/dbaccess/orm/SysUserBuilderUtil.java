package easy4j.infra.dbaccess.orm;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * SysUser 对象赋值工具类
 */
public class SysUserBuilderUtil {

    /**
     * 生成一条测试数据，给SysUser完整赋值
     * @return 填充好字段的SysUser实例
     */
    public static SysUser buildTestSysUser() {
        SysUser user = new SysUser();

        // 1. 各类整型字段赋值
        user.setTinyintAge(25);
        user.setSmallintLevel(3);
        user.setMediumintScore(1280);
        user.setIntLoginCount(36);
        user.setBigintAsset(125000L);
        user.setUintNum(10086);

        // 2. 浮点、定点数（金额用BigDecimal）
        user.setFloatHeight(175.2F);
        user.setDoubleSalary(15800.50D);
        user.setDecimalBalance(new BigDecimal("68920.7520"));

        // 3. 字符串、二进制
        user.setCharUsername("zhangsan001");
        user.setVarcharNickname("张三爱吃火锅");
        user.setBinarySalt("abc123salt456".getBytes());
        user.setVarbinaryPwd("AES加密后的密码字符串".getBytes());

        // 4. 大文本
        user.setTextSign("生活明朗，万物可爱");
        user.setMediumtextArticle("这是一段中等长度的文章内容，可以存放随笔、日志等较长文本");
        user.setLongtextRemark("超级长的备注信息，适用于合同、长篇说明、日志详情等超大文本场景...");

        // 5. 日期时间
        user.setDateBirth(LocalDate.of(1999, 5, 20));
        user.setTimeWork(LocalTime.of(9, 0, 0));
        // 注册时间直接当前时间
        user.setDatetimeRegister(LocalDateTime.now());
        user.setTimestampLastLogin(LocalDateTime.now());
        user.setYearJoin(2022);

        // 6. BIT、布尔、枚举、SET集合
        user.setBitGender(true); // 1男
        user.setIsVip(true);
        user.setEnumStatus(UserStatusEnum.NORMAL);

        Set<String> tags = new HashSet<>();
        tags.add("worker");
        tags.add("boss");
        user.setSetTag(tags);

        // 7. JSON扩展对象赋值
       UserExtraInfo extra = new UserExtraInfo();
        extra.setAddress("重庆市沙坪坝区XX街道XX小区");
        extra.setOpenId("wx_123456789abcdef");
        extra.setHobbies(new String[]{"爬山", "看书", "编程"});
        user.setJsonExtInfo(extra);

        // 主键自增入库后数据库自动生成，代码里一般不手动setId
        // user.setId(1L);

        return user;
    }

    /**
     * 可自定义入参的灵活赋值方法
     */
    public static SysUser buildCustomUser(String username, String nickName, Integer age, BigDecimal balance) {
        SysUser user = new SysUser();
        user.setCharUsername(username);
        user.setVarcharNickname(nickName);
        user.setTinyintAge(age);
        user.setDecimalBalance(balance);
        user.setDatetimeRegister(LocalDateTime.now());
        user.setEnumStatus(UserStatusEnum.NORMAL);
        user.setIsVip(false);
        return user;
    }

    // 测试main方法
    public static void main(String[] args) {
        // 方式1：直接获取完整测试对象
        SysUser testUser = buildTestSysUser();
        System.out.println("赋值完成用户昵称：" + testUser.getVarcharNickname());

        // 方式2：自定义参数创建
        SysUser customUser = buildCustomUser("lisi9527", "李四", 28, new BigDecimal("23500.00"));
        System.out.println("自定义用户账号：" + customUser.getCharUsername());
    }
}