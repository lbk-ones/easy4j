package easy4j.module.jpa.base;

import easy4j.module.base.exception.EasyException;
import easy4j.module.jpa.Comment;
import easy4j.module.jpa.helper.StringTrimHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 主键id
 * 创建时间
 * 创建人
 * 更新时间
 * 更新人
 * 删除时间
 * 是否启用
 * @author bokun
 * @date 2023/5/4
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@Slf4j
public abstract class BaseEntityNoId implements Serializable {

	// 创建人
	@CreatedBy
	@Column(name = "create_by",updatable = false)
	@Comment("创建人 没有则为线程名称")
	@Schema(description = "创建人")
	private String createBy;


	@CreatedDate
	@Column(name = "create_time",updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Comment("创建时间")
	@Schema(description = "创建时间")
	private Date createTime;


	// 最后更新的人
	@LastModifiedBy
	@Column(name = "last_update_by")
	@Comment("最后更新的人")
	@Schema(description = "最后更新的人")
	private String lastUpdateBy;

	// 最后更新的时间
	@LastModifiedDate
	@Column(name = "last_update_time")
	@Temporal(TemporalType.TIMESTAMP)
	@Comment("最后更新的时间")
	@Schema(description = "最后更新的时间")
	private Date lastUpdateTime;

	// 删除的时间
	@Column(name = "del_time")
	@Temporal(TemporalType.TIMESTAMP)
	@Comment("删除的时间")
	@Schema(description = "删除的时间")
	private Date delTime;

	@Column(name = "is_enabled", nullable=false)
	@Comment("-1删除、0禁用、1可用")
	@Schema(description = "-1删除、0禁用、1可用")
	private int isEnabled = 1;	//-1表示记录删除、0表示记录禁用、1表示记录可用的


	@Column(name = "version")
	@Comment("版本号")
	@Schema(description = "版本号")
	private int version;

	protected void trim() throws EasyException {
		try {
			StringTrimHelper.trim(this);
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw new EasyException(e.getMessage());
		}
	}

}
