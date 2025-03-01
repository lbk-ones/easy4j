package easy4j.module.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import easy4j.module.base.header.EasyResult;
import easy4j.module.base.utils.BusCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalFallbackHandler {

    public static EasyResult defaultFallback(Throwable throwable) {
        if(!(throwable instanceof BlockException)){
            return EasyResult.toI18n(throwable);
        }else{
            BlockException ex = (BlockException) throwable;
            String resourceName = ex.getRule().getResource();
            String errorMsg = String.format("资源 [%s] 触发限流规则: %s", resourceName, ex.getClass().getSimpleName());
            log.error(errorMsg);
            if (ex instanceof FlowException) {
                errorMsg = BusCode.A00022;
            } else if (ex instanceof DegradeException) {
                errorMsg = BusCode.A00023;
            }
            return EasyResult.parseFromI18n(1,errorMsg);
        }

    }  
}  
