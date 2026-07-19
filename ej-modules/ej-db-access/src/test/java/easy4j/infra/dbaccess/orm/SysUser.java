package easy4j.infra.dbaccess.orm;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/**
 * 系统用户实体 对应sys_user表
 */
@Data
@JdbcTable(name = "sys_user")
public class SysUser {

    /** 主键自增ID */
    @JdbcColumn(isPrimaryKey = true,autoIncrement = true)
    private Long id;

    /** 年龄 */
    private Integer tinyintAge;

    /** 会员等级 */
    private Integer smallintLevel;

    /** 积分 */
    private Integer mediumintScore;

    /** 登录次数 */
    private Integer intLoginCount;

    /** 资产总额 */
    private Long bigintAsset;

    @TableField
    /** 无符号数字 */
    private Integer uintNum;

    /** 身高 单精度浮点 */
    private Float floatHeight;

    /** 月薪 双精度浮点 */
    private Double doubleSalary;

    /** 账户余额 定点数 必须BigDecimal */
    private BigDecimal decimalBalance;

    /** 固定长度用户名 */
    private String charUsername;

    /** 昵称 */
    private String varcharNickname;

    /** 密码盐 BINARY二进制 */
    private byte[] binarySalt;

    /** 加密密码 VARBINARY */
    private byte[] varbinaryPwd;

    /** 个人简介 TEXT */
    private String textSign;

    /** 长文章 MEDIUMTEXT */
    private String mediumtextArticle;

    /** 超长备注 LONGTEXT */
    private String longtextRemark;

    /** 出生日期 DATE */
    private LocalDate dateBirth;

    /** 每日工作时长 TIME */
    private LocalTime timeWork;

    /** 注册时间 DATETIME */
    private LocalDateTime datetimeRegister;

    /** 最后登录时间 TIMESTAMP */
    private LocalDateTime timestampLastLogin;

    /** 入职年份 YEAR */
    private Integer yearJoin;

    /** BIT(1) 0女 1男 */
    private Boolean bitGender;

    /** 布尔是否VIP */
    private Boolean isVip;

    /** 用户状态 枚举映射 */
    private UserStatusEnum enumStatus;

    /** SET集合标签 数据库逗号分隔字符串转Set */
    private Set<String> setTag;

    private UserExtraInfo jsonExtInfo;
}