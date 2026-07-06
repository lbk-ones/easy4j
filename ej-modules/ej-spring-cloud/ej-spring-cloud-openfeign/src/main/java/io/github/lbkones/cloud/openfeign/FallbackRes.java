package io.github.lbkones.cloud.openfeign;

/**
 * feign的响应体可以实现这个接口
 */
public interface FallbackRes {

    Object fallbackRes(Throwable e);


}
