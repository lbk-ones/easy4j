package easy4j.infra.dbaccess.domains;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * <p>
 * 任务定义表，存储任务的基本配置信息
 * </p>
 *
 * @author bokun.li
 * @since 2025-09-13
 */
@Getter
@Setter
@ToString
@TableName("ssc_meta_job_definition")
@Schema(name = "MetaJobDefinition", description = "任务定义表，存储任务的基本配置信息")
public class MetaJobDefinition  implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务定义ID，主键
     */
    @Schema(description = "任务定义ID，主键")
    @TableId(value = "job_definition_id", type = IdType.AUTO)
    private Integer jobDefinitionId;

    /**
     * 任务名称
     */
    @Schema(description = "任务名称")
    private String jobName;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述")
    private String jobDescription;

    /**
     * 执行计划，cron表达式
     */
    @Schema(description = "执行计划，cron表达式")
    private String executionPlan;

    /**
     * 分类分级引用CODE
     */
    @Schema(description = "分类分级引用CODE")
    private String classificationCode;

    /**
     * 分类分级引用NAME
     */
    @Schema(description = "分类分级引用NAME")
    private String classificationName;

    /**
     * 资源目录ID
     */
    @Schema(description = "资源目录ID")
    private Long resourceDirectoryId;

    /**
     * 模板ID
     */
    @Schema(description = "模板ID")
    private Integer templateId;

    /**
     * 是否将表备注生成表中文名
     */
    @Schema(description = "是否将表备注生成表中文名")
    private Boolean tableCommentAsCnName;

    /**
     * 是否将字段备注生成字段中文名
     */
    @Schema(description = "是否将字段备注生成字段中文名")
    private Boolean columnCommentAsCnName;

    /**
     * 是否启用，1启用0禁用
     */
    @Schema(description = "是否启用，1启用0禁用")
    private Integer isEnabled;

    /**
     * 是否删除，1是删除0是未删除
     */
    @Schema(description = "是否删除，1是删除0是未删除")
    private Integer isDeleted;


    /**
     * 数据源ID
     */
    @Schema(description = "要识别的数据源ID")
    private Integer dataSourceId;

    /**
     * 数据库名称
     */
    @Schema(description = "要识别的数据库名称")
    private String catalogName;

}
