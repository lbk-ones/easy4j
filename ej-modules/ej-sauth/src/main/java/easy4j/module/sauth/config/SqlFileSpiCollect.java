package easy4j.module.sauth.config;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.dbaccess.SqlFileEnums;
import easy4j.infra.dbaccess.SqlFileSpi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class SqlFileSpiCollect implements SqlFileSpi {

    @Override
    public List<SqlFileEnums> collect() {
        return checkSf();
    }

    public static List<SqlFileEnums> checkSf(){
        List<SqlFileEnums> objects = ListTs.newList();
        boolean property = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_ENABLE, boolean.class);
        boolean isServer = Easy4j.getProperty(SysConstant.EASY4J_SAUTH_IS_SERVER, boolean.class);
        if (property && isServer) {
            // must set user impl type
            String type = Easy4j.getRequiredProperty(SysConstant.EASY4J_SIMPLE_AUTH_USER_IMPL_TYPE);
            if (StrUtil.equals(type, SP.DEFAULT)) {
                objects.add(SqlFileEnums.DB_AUTH_USER);
            }
            objects.add(SqlFileEnums.DB_AUTH_USER_SESSION);
            //DBAccessFactory.autoDDL(SecuritySession.class);
            log.info(SysLog.compact("sauth module begin init...  user impl type ---> " + type));
        }
        return objects;
    }
}
