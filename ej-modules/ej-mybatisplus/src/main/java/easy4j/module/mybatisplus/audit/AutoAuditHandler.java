/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.mybatisplus.audit;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.user.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

/**
 * 自动审计
 * @author bokun.li
 * @date 2023/11/18
 */
public class AutoAuditHandler implements MetaObjectHandler {

    public static final String DEFAULT_OPERATOR = "system";

    public static final String CREATE_BY = "createBy";
    public static final String CREATE_NAME = "createName";
    public static final String UPDATE_BY = "updateBy";
    public static final String UPDATE_NAME = "updateName";
    public static final String CREATE_DATE = "createDate";
    public static final String UPDATE_DATE = "updateDate";


    private final Logger log  = LoggerFactory.getLogger(AutoAuditHandler.class);

    public UserContext getUserContext(){
        Easy4jContext context = Easy4j.getContext();
        Optional<Object> threadHashValue = context.getThreadHashValue(UserContext.USER_CONTEXT_NAME, UserContext.USER_CONTEXT_NAME);
        if (threadHashValue.isPresent()) {
            Object o = threadHashValue.get();
            return (UserContext) o;
        }
        return null;
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
        try{
            UserContext userContext = this.getUserContext();
            String userName = userContext.getUserName();
            String userNameCn = userContext.getUserNameCn();
            if (metaObject.hasSetter(CREATE_BY)) {
                this.setFieldValByName(CREATE_BY, userName, metaObject);
            }
            if (metaObject.hasSetter(CREATE_NAME)) {
                this.setFieldValByName(CREATE_NAME, userNameCn, metaObject);
            }
            if (metaObject.hasSetter(UPDATE_BY)) {
                this.setFieldValByName(UPDATE_BY, userName, metaObject);
            }
            if (metaObject.hasSetter(UPDATE_NAME)) {
                this.setFieldValByName(UPDATE_NAME, userNameCn, metaObject);
            }
        }catch (Exception ignored){

        }

    }


    @Override
    public void updateFill(MetaObject metaObject) {
        try{
            UserContext userContext = this.getUserContext();
            String userName = userContext.getUserName();
            String userNameCn = userContext.getUserNameCn();
            if (metaObject.hasSetter(UPDATE_BY)) {
                this.setFieldValByName(UPDATE_BY, userName, metaObject);
            }
            if (metaObject.hasSetter(UPDATE_NAME)) {
                this.setFieldValByName(UPDATE_NAME, userNameCn, metaObject);
            }
            if (metaObject.hasSetter(UPDATE_DATE)) {
                this.setFieldValByName(UPDATE_DATE, new Date(), metaObject);
            }
        }catch (Exception ignored){

        }

    }
}
