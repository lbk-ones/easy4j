package easy4j.infra.dbaccess.orm;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举 映射数据库ENUM('NORMAL','LOCK','DELETE')
 */
@Getter
public enum UserStatusEnum {

    NORMAL("NORMAL", "正常"),
    LOCK("LOCK", "锁定"),
    DELETE("DELETE", "已删除");

    /** 数据库存储值 */
    @EnumValue
    @JsonValue
    private final String code;
    private final String desc;

    UserStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}