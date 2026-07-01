package io.github.lbkones.encryption.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 加密的请求体
 */
public class EncryptedRequest<T> {

    @JsonProperty("data")
    private String data;

    public EncryptedRequest() {
    }

    public EncryptedRequest(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
