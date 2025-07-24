/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy4j.infra.webmvc;

import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * base controller
 */
public class BaseController {

    /**
     * check params
     *
     * @param pageNo   page number
     * @param pageSize page size
     * @throws EasyException exception
     */
    public void checkPageParams(int pageNo, int pageSize) throws EasyException {
        if (pageNo <= 0) {
            throw new EasyException("Not Valid PageNo");
        }
        if (pageSize <= 0) {
            throw new EasyException("Not Valid PageSize");
        }
    }

    /**
     * get ip address in the http request
     *
     * @param request http servlet request
     * @return client ip address
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader(SysConstant.HTTP_X_FORWARDED_FOR);

        if (StringUtils.isNotEmpty(clientIp) && !clientIp.equalsIgnoreCase(SysConstant.HTTP_HEADER_UNKNOWN)) {
            int index = clientIp.indexOf(SP.COMMA);
            if (index != -1) {
                return clientIp.substring(0, index);
            } else {
                return clientIp;
            }
        }

        clientIp = request.getHeader(SysConstant.HTTP_X_REAL_IP);
        if (StringUtils.isNotEmpty(clientIp) && !clientIp.equalsIgnoreCase(SysConstant.HTTP_HEADER_UNKNOWN)) {
            return clientIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * success
     *
     * @return success result code
     */
    public EasyResult success() {
        return EasyResult.ok(null);
    }

    /**
     * success does not need to return data
     *
     * @param msg success message
     * @return success result code
     */
    public EasyResult success(String msg) {
        return EasyResult.parseFromI18n(0,msg);
    }





}
