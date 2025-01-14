package easy4j.modules.ltl.transactional.component;


import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import easy4j.module.seed.CommonKey;
import easy4j.module.seed.leaf.LeafGenIdService;
import easy4j.modules.ltl.transactional.LocalMessage;
import easy4j.modules.ltl.transactional.LtTransactional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Map;

// bk.li
@Aspect
@Component
public class LtTransactionalAspect {

    @Autowired
    LeafGenIdService leafGenIdService;

    @Autowired
    LtlTransactionService ltlTransactionDao;

    /**
     * 事务开始之前写一条信息到本地表
     * 随着业务方法的提交这条本地表的信息状态变为已发送
     * 本地事务提交之后 开始调用其他服务的方法
     * 如果这个方法调用成功 那么就删除这条信息
     * 如果调用失败 将重试指定次数 默认3次 3次重试都结束还没成功
     * 那么就不处理 等定时任务去补偿补偿成功删掉本地消息
     */
    @Around("@annotation(easy4j.modules.ltl.transactional.LtTransactional)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {

            LocalMessage finalLocalMessage = preMessage((MethodInvocationProceedingJoinPoint) joinPoint);

            registerCallBack(joinPoint, finalLocalMessage);

            return finalLocalMessage.getObject();
        }
        return joinPoint.proceed();

    }

    private void registerCallBack(ProceedingJoinPoint joinPoint, LocalMessage finalLocalMessage) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 事务已提交
                finalLocalMessage.setStatus(LocalMessage.SENT);
                ltlTransactionDao.insertOrUpdateLocalMessage(finalLocalMessage);
                try {
                    Object proceed = joinPoint.proceed();
                    finalLocalMessage.setObject(proceed);
                    ltlTransactionDao.delete(finalLocalMessage);
                } catch (Throwable e) {
                    // 失败
                    finalLocalMessage.setStatus(LocalMessage.FAILED);
                    ltlTransactionDao.insertOrUpdateLocalMessage(finalLocalMessage);
                    Integer retryCount = finalLocalMessage.getRetryCount();
                    while (retryCount>0){
                        try {
                            if(null != ltlTransactionDao.findById(finalLocalMessage.getMsgId())){
                                joinPoint.proceed();
                                ltlTransactionDao.delete(finalLocalMessage);
                            }
                            break;
                        } catch (Throwable ex) {
                            retryCount--;
                            finalLocalMessage.setRetryCount(retryCount);
                            ltlTransactionDao.insertOrUpdateLocalMessage(finalLocalMessage);
                        }
                    }
                }
            }
        });
    }


    private LocalMessage preMessage(MethodInvocationProceedingJoinPoint joinPoint) {
        LocalMessage localMessage = new LocalMessage();
        localMessage.setMsgId(CommonKey.gennerString());
        localMessage.setCreateDate(LocalDateTimeUtil.now());
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LtTransactional annotation = method.getAnnotation(LtTransactional.class);
        String s = annotation.businessKey();
        localMessage.setBusinessKey(s);
        localMessage.setRetryCount(annotation.retryCount());
        String[] parameterNames = signature.getParameterNames();
        Map<String,String> paramMap = Maps.newHashMap();
        for (int i = 0; i < parameterNames.length; i++) {
            String parameterName = parameterNames[i];
            Object arg = args[i];
            if(!"java.lang.Object".equals(arg.getClass().getName())){
                if (!paramMap.containsKey(parameterName)) {
                    paramMap.put(parameterName, JSON.toJSONString(arg));
                }else{
                    paramMap.put(parameterName+i, JSON.toJSONString(arg));
                }
            }
        }
        localMessage.setContent(JSON.toJSONString(paramMap));
        localMessage.setStatus(LocalMessage.PENDING);
        localMessage.setBeanName(annotation.baenName());
        localMessage.setBeanMethod(annotation.beanMethod());
        ltlTransactionDao.insertOrUpdateLocalMessage(localMessage);

        return localMessage;
    }
}
