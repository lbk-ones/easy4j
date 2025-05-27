package easy4j.module.sauth.core;

import easy4j.module.sauth.domain.SecurityUserInfo;

public interface EncryptionService {

    String encrypt(String str, SecurityUserInfo securityUser);

    String decrypt(String str, SecurityUserInfo securityUser);

}
