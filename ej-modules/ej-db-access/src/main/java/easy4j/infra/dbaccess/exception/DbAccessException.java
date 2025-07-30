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
package easy4j.infra.dbaccess.exception;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysLog;
/**
 * 未知sql异常类型简单封装
 *
 * @author bokun.li
 * @date 2025/7/30
 */
public class DbAccessException extends RuntimeException{

    public String reason;
    public String sqlState;
    public Throwable cause;

    public DbAccessException(String msg,String sqlState,Throwable e) {
        super(msg);
        this.reason = msg;
        this.sqlState = sqlState;
        this.cause = e;
        String causeStr = "";
        if(e != null){
            causeStr = e.getMessage();
            e.printStackTrace();
        }
        Easy4j.error(SysLog.compact("出现未知sql异常【"+sqlState+"】:"+causeStr));
    }


}
