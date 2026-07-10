package easy4j.infra.dbaccess;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.domain.OperationLogs;
import lombok.Getter;

/**
 * 记录所有系统要用的自动建表信息
 */
@Getter
public enum SqlFileEnums {

    DB_LOG("db/log","日志记录",true,null),
    DB_SIMPLE_LOCK("db/simplelock","数据库锁",true,null),
    DB_IDEMPOTENT("db/idempotent","幂等",true,null),
    DB_LEAF("db/leaf","美团分布式主键需要建的表",true,null),
    DB_SNOW_IP("db/snowip","雪花算法存IP的",true,null),
    DB_AUTH_USER("db/auth-user","权限用户信息",false,null),
    DB_AUTH_USER_SESSION("db/auth","权限用户会话",false,null),
    DB_FENCE("db/fence","seata的Tcc消息表",false,null),
    DB_OPERATE_LOG("","记录操作日志的日志表",false, OperationLogs.class),
    DB_LT("db/lt","本地消息表实现",false,null),
    ;
    private final String path;
    private final String desc;
    // 是否固定执行,如果不是那么可能会跟随其他属性来动态判断
    private final boolean isFixed;
    // 是否自动将class对象转为建表语句
    private final Class<?> autoDDLClass;

    SqlFileEnums(String path, String desc,boolean isFixed,Class<?> autoDDLClass) {
        this.path = path;
        this.desc = desc;
        this.isFixed = isFixed;
        this.autoDDLClass = autoDDLClass;
    }

    public static SqlFileEnums of(String path){
        for (SqlFileEnums value : SqlFileEnums.values()) {
            String path1 = value.getPath();
            if(StrUtil.equals(path1,path)){
             return value;
            }
        }
        return null;
    }

}
