package easy4j.infra.context.api.sca;

import easy4j.infra.common.header.EasyResult;

public interface Easy4jNacosInvokerApi {


    <T> EasyResult<T> get(NacosInvokeDto nacosInvokeDto, Class<T> tClass);


    <T> EasyResult<T> post(NacosInvokeDto nacosInvokeDto, Class<T> tClass);


    <T> EasyResult<T> put(NacosInvokeDto nacosInvokeDto, Class<T> tClass);


    <T> EasyResult<T> delete(NacosInvokeDto nacosInvokeDto, Class<T> tClas);

}
