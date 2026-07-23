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


    private long beginTime;


    private Date exeBeginTime;
    private long exeTime;

    /**
     * 受影响的条数
     */
    private int effectRows;

}
