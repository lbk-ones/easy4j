package easy4j.module.dubbo3;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;


/**
 * DUBBO jaeger链路追踪
 * @author bokun.li
 * @date 2024/1/10
 */
@Activate(
        group = {CommonConstants.CONSUMER,CommonConstants.PROVIDER},
        order = -1
)
/**
 * DubboTraceFilter
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class DubboTraceFilter implements Filter {

    static boolean enabledJaeger;

    static {
        try {
            DubboTraceFilter.class.getClassLoader().loadClass("io.jaegertracing.internal.JaegerTracer");
            enabledJaeger = true;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String DUBBO_TRACE_ID = "uber-trace-id";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if(enabledJaeger){
            RpcContext context = RpcContext.getServiceContext();
            boolean consumerSide = context.isConsumerSide();
            return invokeDubboTrace(consumerSide, invoker, invocation);
        }
        return invoker.invoke(invocation);
    }

    /**
     * dubbo链路传递
     * @param isConsumer
     * @param invoker
     * @param invocation
     */
    public Result invokeDubboTrace(boolean isConsumer, Invoker<?> invoker, Invocation invocation){
        Result invoke = null;
        Tracer globalTracer = SpringUtil.getBean(Tracer.class);
        String methodName = invocation.getMethodName();
        String interfaceName = invocation.getInvoker().getInterface().getName();
        String spanName = interfaceName+"_"+methodName;
        //If it's a consumer, then it can only come from HTTP, either from the HTTP gateway, or from the TTL parent-child thread
        if(isConsumer){
            Span start = globalTracer.activeSpan();
            if(start == null){
                log.info(spanName+"无法追踪到链路");
                return invoker.invoke(invocation);
            }
            //Span start = globalTracer.activeSpan();
            SpanContext context = start.context();
            invocation.setAttachment(DUBBO_TRACE_ID,context.toString());
            try{
                invoke = invoker.invoke(invocation);
            }catch (Throwable e){
                extractedExceptionCode(e, start);
                Map<String, String> stringStringMap = TraceUtils.logsForException(e);
                start.log(stringStringMap);
            }finally {
                start.finish();
            }
        }else{
            // 从dubbo2.js 、 dubbo-client 、 http网关 过来的
            String attachment = invocation.getAttachment(DUBBO_TRACE_ID);
            if(StrUtil.isNotBlank(attachment)){
                Map<String,String> map = Maps.newHashMap();
                map.put(DUBBO_TRACE_ID,attachment);
                TextMapAdapter entries = new TextMapAdapter(map);
                SpanContext extract = globalTracer.extract(Format.Builtin.TEXT_MAP, entries);
                Span start = globalTracer
                        .buildSpan("provider_"+spanName)
                        .asChildOf(extract)
                        .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
                        .start();
                try(Scope scope = globalTracer.activateSpan(start)){
                    invoke = invoker.invoke(invocation);
                }catch (Throwable e){
                    extractedExceptionCode(e, start);
                    Map<String, String> stringStringMap = TraceUtils.logsForException(e);
                    start.log(stringStringMap);
                }finally {
                    start.finish();
                }
            }else{
                invoke = invoker.invoke(invocation);
            }
        }
        return invoke;
    }

    private static void extractedExceptionCode(Throwable e, Span start) {
        if(e instanceof RpcException){
            RpcException e1 = (RpcException) e;
            if(e1.isTimeout()){
                start.setTag("rpc_status",500);
                start.setTag("status_msg","dubbo调用超时");
            }else if(e1.isSerialization()){
                start.setTag("rpc_status",500);
                start.setTag("status_msg","dubbo序列化异常");
            }else if(e1.isNetwork()){
                start.setTag("rpc_status",500);
                start.setTag("status_msg","远程调用网络异常");
            }else{
                start.setTag("rpc_status",500);
                start.setTag("status_msg","错误代码:"+e1.getCode());
            }
        }
    }
}