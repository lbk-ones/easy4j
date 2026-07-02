package io.github.lbkones.encryption.annotation;

import io.github.lbkones.encryption.config.EncryptionConfiguration;
import io.github.lbkones.encryption.util.SUtils;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(value = {EncryptionConfiguration.class, SUtils.class})
public @interface EnableEasy4jEncryption {
}
