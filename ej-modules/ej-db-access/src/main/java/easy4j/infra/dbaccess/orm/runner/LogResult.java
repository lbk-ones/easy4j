package easy4j.infra.dbaccess.orm.runner;

import lombok.Data;

import java.util.Date;

@Data
public class LogResult {


    private String sql;

    /**
     * 耗时如果是批量执行的那么这个是所有加起来的耗时，并不能看出单个sql的耗时
     */
    private long costTime;


    // sql解析前的时间
    private long beginTime;


    // 真正执行的开始时间
    private Date exeBeginTime;


    // sql真正执行的耗时
    private long exeTime;

    /**
     * 受影响的条数,或者说count的结果
     */
    private int effectRows;

}
