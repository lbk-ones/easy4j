package easy4j.module.mybatisplus.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * 几个基本的审计字段
 * 给dto使用
 *
 * @author bokun.li
 * @date 2025/7/23
 */
@Data
public class BaseAuditDto implements Serializable {
    /**
     * 创建人编码
     */
    @Schema(description = "创建人编码")
    private String createBy;

    /**
     * 创建人名称
     */
    @Schema(description = "创建人名称")
    private String creatorName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createdTime;

    /**
     * 更新人编码
     */
    @Schema(description = "更新人编码")
    private String updateBy;

    /**
     * 最新更新时间
     */
    @Schema(description = "最新更新时间")
    private Date lastUpdateTime;
}
