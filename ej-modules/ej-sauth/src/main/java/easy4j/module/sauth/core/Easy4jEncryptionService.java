package easy4j.module.sauth.core;

import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.stereotype.Service;

@Service
public class Easy4jEncryptionService implements EncryptionService {

    @Override
    public String encrypt(String str, SecurityUserInfo securityUser) {
        return null;
    }

    @Override
    public String decrypt(String str, SecurityUserInfo securityUser) {
        return null;
    }
}
