package easy4j.modules.ltl.transactional.component;


import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Maps;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.starter.EnvironmentHolder;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SqlFileExecute;
import easy4j.module.base.utils.SysLog;
import easy4j.module.seed.CommonKey;
import easy4j.module.seed.leaf.LeafGenIdService;
import easy4j.modules.ltl.transactional.LocalMessage;
import easy4j.modules.ltl.transactional.LtTransactional;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// bk.li
@Aspect
@Slf4j
public class LtTransactionalAspect implements InitializingBean, CommandLineRunner {
    private DBAccess dbAccess;
    private static final ConcurrentLinkedDeque<LocalMessage> LINKED_DEQUE = new ConcurrentLinkedDeque<>();

    private static final List<LocalMessage> POLL_LIST = new CopyOnWriteArrayList<>();

    private static final Object LOCK_OBJECT = new Object();
    private static final AtomicBoolean IS_ING = new AtomicBoolean(false);

    @Override
    public void run(String... args) throws Exception {

        DBAccessFactory.INIT_DB_FILE_PATH.add("db/lt");
        dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));

        try {
            processFailedDatas();
        } catch (Exception e) {
            log.error("本地消息表初始化任务失败", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // scan queue for records to db
        Thread thread = new Thread(() -> {
            while (true) {
                batchPoll();
                try {
                    TimeUnit.SECONDS.sleep(5L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("scan-linked-queue-thread");
        thread.start();

        Thread thread2 = new Thread(() -> {
            while (true) {
                boolean b = IS_ING.get();
                if (!b) {
                    IS_ING.set(true);
                    synchronized (LOCK_OBJECT) {
                        try {
                            LOCK_OBJECT.wait();
                        } catch (InterruptedException ignored) {
                        }
                        log.info("本地消息回放线程被唤醒**");
                        batchPoll();
                        processFailedDatas();
                    }
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(5L);
                    } catch (InterruptedException ignored) {

                    }
                }
            }
        });
        thread2.setDaemon(true);
        thread2.setName("reply-ltl-thread");
        thread2.start();

        log.info(SysLog.compact("本地消息表初始化完毕"));
    }

    private synchronized static void batchPoll() {
        LocalMessage poll = LINKED_DEQUE.poll();
        if (null != poll) {
            POLL_LIST.add(poll);
            if (POLL_LIST.size() >= 100) {
                pollProcessBatch();
            }
        } else if (!POLL_LIST.isEmpty()) {
            pollProcessBatch();
        }
    }


    public static void processFailedDatas() {
        try {
            if (!IS_ING.get()) {
                IS_ING.set(true);
            }
            LtlTransactionService bean = SpringUtil.getBean(LtlTransactionService.class);
            List<LocalMessage> localMessages = bean.findAllFailed();
            if (localMessages.isEmpty()) {
                return;
            }
            log.info("本次补偿本地消息:" + localMessages.size() + "条");
            bean.freezeAll(localMessages);
            for (LocalMessage localMessage : localMessages) {
                List<Object> objects = ListTs.newArrayList();
                try {
                    String beanName = localMessage.getBeanName();
                    Object bean1 = SpringUtil.getBean(beanName);
                    if (StrUtil.isBlank(beanName)) {
                        log.error("本地消息表补偿失败,beanName为空,msgId:" + localMessage.getMsgId());
                        continue;
                    }
                    String content = localMessage.getContent();
                    if (StrUtil.isNotBlank(content)) {
                        JSONObject jsonObject = JSON.parseObject(content);
                        for (String clazzKey : jsonObject.keySet()) {
                            Object o = jsonObject.get(clazzKey);
                            if (o == null) {
                                Class<?> aClass = Class.forName(clazzKey);
                                Object o1 = ReflectUtil.newInstance(aClass);
                                objects.add(o1);
                            } else {
                                objects.add(o);
                            }
                        }
                        ReflectUtil.invoke(bean1, localMessage.getBeanMethod(), objects.toArray(new Object[]{}));
                    } else {
                        ReflectUtil.invoke(bean1, localMessage.getBeanMethod());
                    }

                    bean.delete(localMessage);
                } catch (Throwable e) {
                    log.error("本地消息补偿失败,调用参数:" + JSON.toJSONString(objects));
                    log.error("本地消息补偿失败", e);
                }
            }
        } finally {
            IS_ING.set(false);
        }
    }

    // batch flush to db
    private static void pollProcessBatch() {
        if (POLL_LIST.isEmpty()) {
            return;
        }
        LtlTransactionService bean = SpringUtil.getBean(LtlTransactionService.class);
        if (null == bean) {
            return;
        }
        for (LocalMessage localMessage : POLL_LIST) {
            try {
                localMessage.setStatus(LocalMessage.FAILED);
                bean.insertOrUpdateLocalMessage(localMessage);
                POLL_LIST.removeIf(e -> e.getMsgId().equals(localMessage.getMsgId()));
            } catch (DuplicateKeyException duplicateKeyException) {
                try {
                    localMessage.setMsgId(CommonKey.gennerString());
                    bean.insertOrUpdateLocalMessage(localMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    @Autowired
    LeafGenIdService leafGenIdService;

    @Autowired
    LtlTransactionService ltlTransactionDao;

    /**
     * 如果有多个本地消息业务并存 一个成功 一个失败
     * 主要解决的问题是 我成功了 但是消息推送失败了 那么就需要本地消息来回放或者后台回放
     * 本地消息表逻辑
     * 1. 组装消息,如果不满足条件则跳过
     * 2. 调用执行方法
     * 3. 如果成功，则更改标志为成功，且不进行后续操作
     * 4. 如果失败，则重试
     * 5. 如果重试失败，则更改标志为为失败
     * 6. 如果当前未处于事务中，则直接放入队列中等待写入
     * 7. 如果处于事务中，则在事务完成之后判断标志位来决定是否写入队列
     * 8. 队列会被轮询消费 写入库中
     * 9. 错误消息会被唤醒回放
     */
    @Around("@annotation(easy4j.modules.ltl.transactional.LtTransactional)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalMessage finalLocalMessage = preMessage((MethodInvocationProceedingJoinPoint) joinPoint);
        if (null == finalLocalMessage) {
            return joinPoint.proceed();
        }
        boolean hasTransaction = false;
        // has transaction
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            registerCallBack(finalLocalMessage);
            hasTransaction = true;
        }
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
            finalLocalMessage.setStatus(LocalMessage.SENT);
        } catch (Throwable e) {
            String message = e.getMessage();
            finalLocalMessage.setErrorMessage(message);
            Integer retryCount = finalLocalMessage.getRetryCount();
            finalLocalMessage.setStatus(LocalMessage.FAILED);
            while (retryCount > 0) {
                try {
                    log.error(finalLocalMessage.getBeanMethod() + "开始重试----" + retryCount);
                    proceed = joinPoint.proceed();
                    finalLocalMessage.setStatus(LocalMessage.SENT);
                    break;
                } catch (Throwable ex) {
                    finalLocalMessage.setErrorMessage(ex.getMessage());
                    retryCount--;
                }
            }
            if (!hasTransaction && LocalMessage.FAILED.equals(finalLocalMessage.getStatus())) {
                LINKED_DEQUE.offer(finalLocalMessage);
                synchronized (LOCK_OBJECT) {
                    LOCK_OBJECT.notifyAll();
                }
            }

        }
        return proceed;

    }

    private void registerCallBack(LocalMessage finalLocalMessage) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (LocalMessage.FAILED.equals(finalLocalMessage.getStatus())) {
                    // 失败
                    LINKED_DEQUE.offer(finalLocalMessage);
                    synchronized (LOCK_OBJECT) {
                        LOCK_OBJECT.notifyAll();
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
        if (
                StrUtil.isEmpty(s) ||
                        StrUtil.isEmpty(annotation.beanMethod()) ||
                        StrUtil.isEmpty(annotation.baenName())
        ) {
            return null;
        }
        localMessage.setBusinessKey(s);
        localMessage.setBusinessName(annotation.businessName());
        localMessage.setRetryCount(annotation.retryCount());
        String[] parameterNames = signature.getParameterNames();
        Map<String, String> paramMap = Maps.newHashMap();
        for (int i = 0; i < parameterNames.length; i++) {
            Object arg = args[i];
            String name = arg.getClass().getName();
            paramMap.put(name, ObjectUtil.isEmpty(arg) ? null : JSON.toJSONString(arg));
        }
        localMessage.setContent(JSON.toJSONString(paramMap));
        localMessage.setStatus(LocalMessage.PENDING);
        localMessage.setBeanName(annotation.baenName());
        localMessage.setBeanMethod(annotation.beanMethod());

        return localMessage;
    }
}
