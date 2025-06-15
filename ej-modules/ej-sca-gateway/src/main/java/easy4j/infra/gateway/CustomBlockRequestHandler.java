package easy4j.infra.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.utils.BusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 网关限流熔断异常处理器
 */
public class CustomBlockRequestHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        // 根据不同的异常类型返回不同的错误响应
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        EasyResult<Map<String, Object>> objectEasyResult = new EasyResult<>();
        objectEasyResult.setError(1);
        String msg;

        if (ex instanceof FlowException) {
            msg = BusCode.A00022;
        } else if (ex instanceof DegradeException) {
            msg = BusCode.A00024;
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof ParamFlowException) {
            msg = BusCode.A00025;
        } else if (ex instanceof SystemBlockException) {
            msg = BusCode.A00026;
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof AuthorityException) {
            msg = BusCode.A00027;
            status = HttpStatus.FORBIDDEN;
        } else {
            msg = BusCode.A00028;
        }
        EasyResult<Object> objectEasyResult1 = EasyResult.parseFromI18n(1, msg);
        if (ex instanceof BlockException) {
            BlockException ex1 = (BlockException) ex;
            // 添加原始异常信息（生产环境可根据需要移除）
            String resource = ex1.getRule().getResource();
            objectEasyResult1.setData(resource);
        }

        // 返回 JSON 格式的响应
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectEasyResult1.toString());
    }
}