package easy4j.module.sauth.core;

import easy4j.module.sauth.domain.SecurityUserInfo;

/**
 * EncryptionService
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface EncryptionService {

    String encrypt(String str, SecurityUserInfo securityUser);

    String decrypt(String str, SecurityUserInfo securityUser);

}