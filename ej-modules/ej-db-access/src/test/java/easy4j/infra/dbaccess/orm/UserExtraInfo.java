package easy4j.infra.dbaccess.orm;

import lombok.Data;

/**
 * 用户扩展JSON信息
 */
@Data
public class UserExtraInfo {
    /** 收货地址 */
    private String address;
    /** 第三方openId */
    private String openId;
    /** 爱好数组 */
    private String[] hobbies;
}