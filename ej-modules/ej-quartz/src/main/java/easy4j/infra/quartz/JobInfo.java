package easy4j.infra.quartz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.quartz.Job;
import org.quartz.JobDataMap;

import java.util.Date;

@Data
@Schema(description = "任务信息")
public class JobInfo {
    /**
     * 任务名称 一般是任务定义ID
     */
    @Schema(description = "任务名称 一般是任务定义ID")
    private String jobName;
    /**
     * 任务组
     */
    @Schema(description = "任务组 一般是业务类型标识，或者项目标识")
    private String jobGroup;
    /**
     * 任务类
     */
    @Schema(description = "任务类")
    private Class<? extends Job> jobClass;
    /**
     * 任务开始时间
     */
    @Schema(description = "任务开始时间")
    private Date startDate;
    /**
     * 任务结束时间，如果不设置那么就不会结束
     */
    @Schema(description = "任务结束时间，如果不设置那么就不会结束")
    private Date endDate;

    /**
     * cron表达式
     */
    @Schema(description = "cron表达式")
    private String cronTab;

    /**
     * 时区
     */
    @Schema(description = "时区")
    private String timeZone = "Asia/Shanghai";

    /**
     * 任务传递参数
     */
    @Schema(description = "任务传递参数")
    private JobDataMap jobDataMap;
}
