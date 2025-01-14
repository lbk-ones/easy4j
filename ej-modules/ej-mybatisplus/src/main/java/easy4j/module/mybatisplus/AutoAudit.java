package easy4j.module.mybatisplus;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 自动审计
 * @author bokun.li
 * @date 2023/11/18
 */
public class AutoAudit implements MetaObjectHandler {

    public static final String DEFAULT_OPERATOR = "system";

    public static final String CREATE_BY = "createBy";
    public static final String UPDATE_BY = "updateBy";
    public static final String CREATE_DATE = "createDate";
    public static final String UPDATE_DATE = "updateDate";


    private final Logger log  = LoggerFactory.getLogger(AutoAudit.class);

    public String getUserName(){

        return DEFAULT_OPERATOR;
    }
    @Override
    public void insertFill(MetaObject metaObject) {
        boolean updateDate = metaObject.hasSetter(UPDATE_DATE);
        boolean createDate = metaObject.hasSetter(CREATE_DATE);
        if(updateDate || createDate){
            Date date = new Date();
            if(createDate){
                this.setFieldValByName(CREATE_DATE, date, metaObject);
            }
            if(updateDate){
                this.setFieldValByName(UPDATE_DATE, date, metaObject);
            }
        }
        String userName = this.getUserName();
        if (metaObject.hasSetter(CREATE_BY)) {
            this.setFieldValByName(CREATE_BY, userName, metaObject);
        }
        if (metaObject.hasSetter(UPDATE_BY)) {
            this.setFieldValByName(UPDATE_BY, userName, metaObject);
        }
    }


    @Override
    public void updateFill(MetaObject metaObject) {
        String userName = this.getUserName();
        if (metaObject.hasSetter(UPDATE_BY)) {
            this.setFieldValByName(UPDATE_BY, userName, metaObject);
        }
        if (metaObject.hasSetter(UPDATE_DATE)) {
            this.setFieldValByName(UPDATE_DATE, new Date(), metaObject);
        }
    }
}