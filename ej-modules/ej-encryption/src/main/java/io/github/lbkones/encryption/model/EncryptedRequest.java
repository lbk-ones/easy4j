package io.github.lbkones.encryption.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 加密的请求体
 */
@Setter
@Getter
public class EncryptedRequest {

    @JsonProperty("data")
    private String data;

    public EncryptedRequest() {
    }

    public EncryptedRequest(String data) {
        this.data = data;
    }

}
