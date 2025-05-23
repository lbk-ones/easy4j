package easy4j.module.mybatisplus;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.util.Date;

public class AuditDemo implements Serializable {

    // 创建人
    @TableField(value="CREATE_BY",fill = FieldFill.INSERT)
    private String createBy;

    // 创建时间
    @TableField(value="CREATE_DATE",fill = FieldFill.INSERT)
    private Date createDate;

    // 更新人
    @TableField(value="UPDATE_BY",fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    // 更新时间
    @TableField(value="UPDATE_DATE",fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;


    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
