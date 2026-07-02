package io.github.lbkones.encryption.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 加密的响应体
 */
@Setter
@Getter
public class EncryptedResponse {

    @JsonProperty("data")
    private String data;

    public EncryptedResponse() {
    }

    public EncryptedResponse(String data) {
        this.data = data;
    }

}
