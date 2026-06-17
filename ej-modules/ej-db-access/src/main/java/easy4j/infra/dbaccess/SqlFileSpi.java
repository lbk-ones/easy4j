package easy4j.infra.dbaccess;

import java.util.List;

/**
 * 去获取各个模块中需要执行的sql脚本，需要执行的脚本不能被写死，所以用spi获取一下
 * @author bokun.li
 */
public interface SqlFileSpi {

    List<SqlFileEnums> collect();

}
