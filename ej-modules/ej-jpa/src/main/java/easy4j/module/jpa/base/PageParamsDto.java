package easy4j.module.jpa.base;

import easy4j.module.base.exception.EasyException;
import easy4j.module.jpa.annotations.Trim;
import easy4j.module.jpa.constant.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class PageParamsDto extends BaseDto{
    private Integer pageSize = Constant.PAGE_SIZE;

    private Integer pageNo = Constant.PAGE_NUMBER;

    // 关键字
    @Trim
    private String searchKey;

    private String backField1;

    private String backField2;

    private String backField3;

    // 是否锁定
    private String isLock;
    // 所属组织
    private String org;
    // 用户类型
    private String userType;

    // 是否来自 门户 1 就是来自门户
    private String fromPortal;

    @Override
    public void toNewEntityValidate() throws EasyException {

    }

    @Override
    public void toModifyEntityValidate(Date modifyTime) throws EasyException {

    }
}
