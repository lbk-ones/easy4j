package easy4j.infra.gateway;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.header.EasyResult;
import easy4j.infra.common.header.GateWayEasyResult;
import easy4j.infra.common.utils.BusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 网关限流熔断异常处理器
 */
public class CustomBlockRequestHandler implements BlockRequestHandler {


    public String getMsgByCode(String code){

        Field[] fields = ReflectUtil.getFields(BusCode.class, e -> Modifier.isStatic(e.getModifiers()));

        for (Field field : fields) {
            String name = field.getName();
            if(StrUtil.equals(name,code) && field.isAnnotationPresent(Desc.class)){
                return field.getAnnotation(Desc.class).value();
            }
        }
        return "Unknown Error";

    }

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        // 根据不同的异常类型返回不同的错误响应
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String msgCode;

        if (ex instanceof FlowException) {
            msgCode = BusCode.A00022;
        } else if (ex instanceof DegradeException) {
            msgCode = BusCode.A00024;
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof ParamFlowException) {
            msgCode = BusCode.A00025;
        } else if (ex instanceof SystemBlockException) {
            msgCode = BusCode.A00026;
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof AuthorityException) {
            msgCode = BusCode.A00027;
            status = HttpStatus.FORBIDDEN;
        } else {
            msgCode = BusCode.A00028;
        }
        GateWayEasyResult<Object> gateWayEasyResult = new GateWayEasyResult<>();
        gateWayEasyResult.setCode(msgCode);
        gateWayEasyResult.setMessage(getMsgByCode(msgCode));
        if (ex instanceof BlockException) {
            BlockException ex1 = (BlockException) ex;
            // 添加原始异常信息（生产环境可根据需要移除）
            String resource = ex1.getRule().getResource();
            gateWayEasyResult.setData(resource);
        }

        // 返回 JSON 格式的响应
        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(gateWayEasyResult.toString());
    }
}