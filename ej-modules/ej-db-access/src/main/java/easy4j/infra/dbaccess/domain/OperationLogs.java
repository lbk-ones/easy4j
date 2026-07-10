package easy4j.infra.dbaccess.domain;

import easy4j.infra.dbaccess.dynamic.dll.DDLField;
import easy4j.infra.dbaccess.dynamic.dll.DDLTable;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 操作日志表
 * </p>
 *
 * @author easy4j
 * @since 2026年6月30日 17:07:15
 */
@Getter
@Setter
@ToString
@DDLTable(
        tableName = "sys_operation_logs",
        indexes = {
                @DDLIndex(keys = {"module", "action"}),
                @DDLIndex(keys = {"operator_id"}),
                @DDLIndex(keys = {"operator_ip"}),
                @DDLIndex(keys = {"success"}),
                @DDLIndex(keys = {"business_type","business_id"}),
                @DDLIndex(keys = {"created_at"}),
        })
@Schema(name = "OperationLog", description = "操作日志表")
public class OperationLogs implements Serializable {

    @Serial
    private static final long serialVersionUID = -2918090871547133310L;


    /**
     * 日志ID
     */
    @Schema(description = "日志ID")
    @DDLField(name = "id",isAutoIncrement =true,isPrimary = true)
    private Long id;


    /**
     * 响应状态码（HTTP Status）
     */
    @Schema(description = "响应状态码（HTTP Status）")
    @DDLField(name = "response_code")
    private Integer responseCode;


    /**
     * 响应数据（JSON格式，可选）
     */
    @Schema(description = "响应数据（JSON格式，可选）")
    @DDLField(name = "response_data",isLob = true)
    private String responseData;


    /**
     * 错误信息（失败时记录）
     */
    @Schema(description = "错误信息（失败时记录）")
    @DDLField(name = "error_msg",dataLength = 1024)
    private String errorMsg;


    /**
     * 操作人ID（关联用户表）
     */
    @Schema(description = "操作人ID（关联用户表）")
    @DDLField(name = "operator_id")
    private Long operatorId;


    /**
     * 操作模块（如：user, order, system）
     */
    @Schema(description = "操作模块（如：user, order, system）")
    @DDLField(name = "module",dataLength = 64)
    private String module;


    /**
     * 业务流水号
     */
    @Schema(description = "业务流水号")
    @DDLField(name = "business_no",dataLength = 128)
    private String businessNo;


    /**
     * 操作描述（如：修改用户密码）
     */
    @Schema(description = "操作描述（如：修改用户密码）")
    @DDLField(name = "description",dataLength = 512)
    private String description;


    /**
     * 请求参数（JSON格式）
     */
    @Schema(description = "请求参数（JSON格式）")
    @DDLField(name = "request_params",isLob = true)
    private String requestParams;


    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @DDLField(name = "created_at")
    private Date createdAt;


    /**
     * 请求方法（GET/POST/PUT/DELETE）
     */
    @Schema(description = "请求方法（GET/POST/PUT/DELETE）")
    @DDLField(name = "request_method",dataLength = 8)
    private String requestMethod;


    /**
     * 请求URL
     */
    @Schema(description = "请求URL")
    @DDLField(name = "request_url",dataLength = 1024)
    private String requestUrl;


    /**
     * 操作人姓名/昵称
     */
    @Schema(description = "操作人姓名/昵称")
    @DDLField(name = "operator_name",dataLength = 512)
    private String operatorName;


    /**
     * 操作人IP地址
     */
    @Schema(description = "操作人IP地址")
    @DDLField(name = "operator_ip",dataLength = 64)
    private String operatorIp;


    /**
     * 请求Body（JSON格式，大字段）
     */
    @Schema(description = "请求Body（JSON格式，大字段）")
    @DDLField(name = "request_body",isLob = true)
    private String requestBody;


    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @DDLField(name = "updated_at")
    private Date updatedAt;


    /**
     * 是否成功：1-成功，0-失败
     */
    @Schema(description = "是否成功：1-成功，0-失败")
    @DDLField(name = "success")
    private Integer success;


    /**
     * 执行耗时（毫秒）
     */
    @Schema(description = "执行耗时（毫秒）")
    @DDLField(name = "cost_time")
    private Integer costTime;


    /**
     * User-Agent（浏览器/设备信息）
     */
    @Schema(description = "User-Agent（浏览器/设备信息）")
    @DDLField(name = "operator_ua",dataLength = 512)
    private String operatorUa;


    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    @DDLField(name = "business_type",dataLength = 64)
    private String businessType;


    /**
     * 操作动作（如：create, update, delete, login, export）
     */
    @Schema(description = "操作动作（如：create, update, delete, login, export）")
    @DDLField(name = "action",dataLength = 64)
    private String action;


    /**
     * 业务主键（如订单号、用户ID）
     */
    @Schema(description = "业务主键（如订单号、用户ID）")
    @DDLField(name = "business_id",dataLength = 128)
    private String businessId;


}
