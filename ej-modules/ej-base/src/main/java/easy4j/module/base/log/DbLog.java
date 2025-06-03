/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.log;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.context.Easy4jContextFactory;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.header.EasyResult;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.dbaccess.condition.FWhereBuild;
import easy4j.module.base.plugin.dbaccess.condition.WhereBuild;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.plugin.lock.Easy4jSysLock;
import easy4j.module.base.plugin.seed.DefaultEasy4jSeed;
import easy4j.module.base.plugin.seed.Easy4jSeed;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;
import jodd.exception.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;

import java.util.Date;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 日志写入工具类
 * 单例
 *
 * @author bokun.li
 */
@Slf4j
public class DbLog {

    public static final String CONTEXT_THREAD_KEY = "easy4j-sys-log-record-context-key";

    public static final String DB_LOCK_ID = "delete-sys-log-record-lock";
    public static final String DB_LOCK_ID_INIT = "delete-sys-log-record-lock-init";
    private static final DBAccess dbAccess = DBAccessFactory.getDBAccess(JdbcHelper.getDataSource(), false, true);

    private static final ThreadLocal<Deque<SysLogRecord>> threadLocalMap = new TransmittableThreadLocal<>();

    private DbLog() {
        synchronized (DbLog.class) {
            AtomicBoolean orDefault = Easy4j.isInitPreLoadApplication.getOrDefault(DB_LOCK_ID_INIT, new AtomicBoolean(false));
            if (!orDefault.get()) {
                init();
                if (orDefault.compareAndSet(false, true)) {
                    Easy4j.isInitPreLoadApplication.put(DB_LOCK_ID_INIT, orDefault);
                }
            }
        }
    }

    private static class DbLogHolder {
        private static final DbLog INSTANCE = new DbLog();
    }


    public static DbLog getDbLog() {
        return DbLogHolder.INSTANCE;
    }


    /**
     * 写入参数
     *
     * @param consumer
     */
    public synchronized static void set(Consumer<SysLogRecord> consumer) {
        Deque<SysLogRecord> logRecord = threadLocalMap.get();
        Optional.ofNullable(logRecord).map(Deque::peekLast).ifPresent(consumer);
    }

    public static void append(SysLogRecord sysLogRecord) {
        Deque<SysLogRecord> logRecord = threadLocalMap.get();
        if (Objects.isNull(logRecord)) {
            threadLocalMap.set(new ConcurrentLinkedDeque<>());
        }
        Deque<SysLogRecord> sysLogRecords = threadLocalMap.get();
        sysLogRecords.add(sysLogRecord);
    }

    /**
     * @param isPop 是否从最末尾弹出
     * @return
     */
    public synchronized static SysLogRecord getLast(boolean isPop) {
        Deque<SysLogRecord> logRecord = threadLocalMap.get();
        try {
            if (Objects.isNull(logRecord)) {
                return null;
            }
            return logRecord.peekLast();
        } finally {
            if (null != logRecord && isPop) {
                try {
                    logRecord.removeLast();
                } catch (Exception ignored) {
                }
            }
        }

    }

    public synchronized static boolean checkIsEmpty() {
        Deque<SysLogRecord> logRecord = threadLocalMap.get();

        if (null != logRecord) {
            return logRecord.isEmpty();
        }
        return false;
    }

    public static <T> T getParams(Function<SysLogRecord, T> consumer, T defaultVa) {
        Deque<SysLogRecord> logRecord = threadLocalMap.get();
        if (Objects.nonNull(logRecord)) {
            SysLogRecord logRecord1 = logRecord.peekLast();
            if (null == logRecord1) {
                return defaultVa;
            }
            return consumer.apply(logRecord1);
        } else {
            return defaultVa;
        }
    }

    public static void putRemark(String remark) {
        String params = getParams(SysLogRecord::getRemark, "");
        String temP = StrUtil.blankToDefault(params, "") + (StrUtil.isNotBlank(params) ? "\n" : "") + remark;
        set(e -> e.setRemark(temP));
    }

    public static void putParams(String params2) {
        String params = getParams(SysLogRecord::getParams, "");
        String temP = StrUtil.blankToDefault(params, "") + (StrUtil.isNotBlank(params) ? "\n" : "") + params2;
        set(e -> e.setParams(temP));
    }

    public static void putTargetId(String targetId) {
        set(e -> e.setTargetId(targetId));
    }

    public static void putTargetId2(String targetId2) {
        set(e -> e.setTargetId2(targetId2));
    }


    /**
     * @param tag     主题
     * @param tagDesc 主题子标题
     * @param content 内容 一般参数
     */
    private synchronized static void beginLogWith(String tag, String tagDesc, String content) {
        try {
            SysLogRecord logRecord = new SysLogRecord();
            logRecord.setTag(tag);
            logRecord.setTagDesc(tagDesc);
            String traceId = Easy4jContextFactory.getContext().getThreadHashValue(SysConstant.TRACE_ID_NAME, SysConstant.TRACE_ID_NAME).map(Object::toString).orElse("");

            logRecord.setTraceId(traceId);
            logRecord.setParams(content);
            logRecord.setCreateDate(new Date());
            String id = insertDomain(logRecord);
            append(logRecord);
            logRecord.setId(id);
            SysLogRecord last = getLast(false);
            BeanUtil.copyProperties(logRecord, last);


        } catch (Throwable e) {
            log.error("日志写入失败", e);
        }
    }

    // -----------------------------------------------------------------------------


    public static void beginLog(String tag, String tagDesc, String content) {

        beginLogWith(tag, tagDesc, content);

    }

    public static Deque<SysLogRecord> getDeque() {

        return threadLocalMap.get();

    }

    public static void endLog(Throwable throwable) {
        endLogWith(null, null, throwable);
    }

    public static void endLog() {
        endLogWith(null, null, null);
    }

    public static void endLogId(String id) {
        endLogWith(id, null, null);
    }

    public static void endLogByStatus(String status) {

        endLogWith(null, status, null);
    }

    // -----------------------------------------------------------------------------------

    /**
     * 结束这一轮的日志统计 从队列里面拿最后一个
     *
     * @param _id
     * @param status
     * @param throwable
     */
    private static void endLogWith(String _id, String status, Throwable throwable) {
        try {
            SysLogRecord last = getLast(true);
            String s = StrUtil.isBlank(_id) ? last != null ? last.getId() : "" : _id;
            if (StrUtil.isBlank(s) && StrUtil.isNotBlank(_id)) {
                SysLogRecord logRecord1 = new SysLogRecord();
                logRecord1.setId(_id);
                last = dbAccess.selectByPrimaryKey(_id, SysLogRecord.class);
            }
            if (Objects.isNull(last) || StrUtil.isBlank(last.getId())) {
                return;
            }

            Date createDate = last.getCreateDate();
            Date newDate = new Date();
            SysLogRecord logRecord = last.toNewLogRecord();
            if (Objects.nonNull(createDate)) {
                long l = newDate.getTime() - createDate.getTime();
                logRecord.setProcessTime(String.valueOf(l));
            } else {
                logRecord.setProcessTime("-1");
            }
            logRecord.setId(s);
            String _status = StrUtil.blankToDefault(status, "1");
            if (Objects.nonNull(throwable)) {
                _status = "0";
                logRecord.setStatus(_status);
                if (throwable instanceof EasyException) {
                    String message1 = EasyResult.toI18n(throwable).getMessage();
                    logRecord.setErrorInfo(message1);
                } else {
                    logRecord.setErrorInfo(ExceptionUtil.exceptionChainToString(throwable));
                }
            } else {
                logRecord.setStatus(_status);
            }
            dbAccess.updateByPrimaryKeySelective(logRecord, SysLogRecord.class, false);
        } catch (Throwable e) {
            log.error("日志完成写入失败", e);
        } finally {
            removeThread();
        }
    }

    public static void removeThread() {
        if (checkIsEmpty()) {
            threadLocalMap.remove();
        }
    }


    private static String insertDomain(SysLogRecord logRecord) {
        Easy4jContext context = Easy4j.getContext();
        Easy4jSeed easy4jSeed = context.getOrDefault(Easy4jSeed.class, new DefaultEasy4jSeed());
        String id = easy4jSeed.nextIdStr();
        logRecord.setId(id);
        try {
            dbAccess.saveOne(logRecord, SysLogRecord.class);

        } catch (DuplicateKeyException exception) {
            logRecord.setId(easy4jSeed.nextIdStr());
            dbAccess.saveOne(logRecord, SysLogRecord.class);
        }
        return id;
    }

    // 上一次执行时间
    private static final AtomicLong lastExeTime = new AtomicLong(0);

    // 第一次执行之后 默认一天执行一次
    private static final long limitHours = 24 * 60 * 60 * 1000;

    // 服务启动之后第一次执行 默认一个小时
//    private static final long initlimitHours = 60 * 60 * 1000;
    private static final long initlimitHours = 10;

    // 第一次执行过没有
    private static volatile boolean firstEd = false;


    private void init() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    long time = new Date().getTime();
                    if (lastExeTime.get() == 0) {
                        lastExeTime.addAndGet(time);
                    }
                    long l = lastExeTime.get();

                    // 默认保留7天的日志
                    Date startTime = DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -7));
                    if (!firstEd) {
                        if (time > l && (time - l) >= initlimitHours) {
                            clearLog(startTime);
                        }
                    } else {
                        if (time > l && (time - l) >= limitHours) {
                            clearLog(startTime);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(10L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } catch (Exception e) {
                    log.error(DateUtil.formatDateTime(new Date()) + "日志清除失败", e);
                }
            }
        });
        thread.setName("日志守护线程");
        thread.setDaemon(true);
        thread.start();
    }

    public void clearLog(Date startTime) {

        try {
            Easy4jSysLock.lock(DB_LOCK_ID, 5, "to delete log record");
        } catch (Exception e) {
            // 没抢到这一次就跳过
            lastExeTime.addAndGet(new Date().getTime());
            return;
        }

        try {
            long beginTime = System.currentTimeMillis();
            SysLogRecord logRecord = new SysLogRecord();
            logRecord.setCreateDate(new Date());
            logRecord.setTag("日志定时清除");

            WhereBuild lte1 = FWhereBuild.get(SysLogRecord.class)
                    .lte(SysLogRecord::getCreateDate, startTime);

            long i = dbAccess.countByCondition(lte1, SysLogRecord.class);

            dbAccess.deleteByCondition(lte1, SysLogRecord.class);
            logRecord.setRemark("日志定时删除" + i + "条");
            log.info("日志定时删除" + i + "条");
            long endTime = System.currentTimeMillis();
            logRecord.setProcessTime((endTime - beginTime) + "");
            insertDomain(logRecord);
            lastExeTime.addAndGet(new Date().getTime());
            firstEd = true;
        } finally {
            Easy4jSysLock.unLock(DB_LOCK_ID);
        }

    }
}
