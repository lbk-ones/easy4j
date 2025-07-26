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
package easy4j.module.sauth.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;


/**
 * DefaultEncryptionService
 * 通过随机数来进行sha128加密
 *
 * @author bokun.li
 * @date 2025-05
 */
public class DefaultEncryptionService implements EncryptionService {
    @Override
    public String encrypt(String pwd, ISecurityEasy4jUser securityUser) {
        String shalt = securityUser.getPwdSalt();

        if (StrUtil.isBlank(shalt)) {
            throw new EasyException(BusCode.A00043);
        }

        return DigestUtil.sha1Hex(pwd + shalt);
    }

    @Override
    public String decrypt(String str, ISecurityEasy4jUser securityUser) {
        return str;
    }
}
