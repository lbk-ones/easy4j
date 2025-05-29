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
package easy4j.module.base.utils;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.utils.json.JacksonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * SysLog
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class SysLog {
    private static final AtomicBoolean IS_SETTING = new AtomicBoolean(false);

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private static final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private static final List<String> ignorePrintException = ListTs.asList("");
    private static final List<String> ignoreDbPrintLog = ListTs.asList(
            "Table 'sys_work_ip' already exists"
    );

    static {
        ignoreDbPrintLog.addAll(SqlType.getIgnoreStateMent());
    }

    public static void addIgnoreException(String exStr) {
        if (StrUtil.isNotBlank(exStr)) {
            try {
                if (writeLock.tryLock() || writeLock.tryLock(3, TimeUnit.SECONDS)) {
                    ignorePrintException.add(exStr);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }
    }

    public static void addIgnoreDbException(String exStr) {
        if (StrUtil.isNotBlank(exStr)) {
            try {
                if (writeLock.tryLock() || writeLock.tryLock(3, TimeUnit.SECONDS)) {
                    ignoreDbPrintLog.add(exStr);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                writeLock.unlock();
            }
        }
    }

    public static boolean checkDbPrintException(String message, boolean isContain) {
        boolean isExists = false;
        if (StrUtil.isBlank(message)) {
            return isExists;
        }
        try {
            if (readLock.tryLock() || readLock.tryLock(3, TimeUnit.SECONDS)) {
                for (String s : ignoreDbPrintLog) {
                    if (StrUtil.isNotBlank(s)) {
                        if (isContain && message.contains(s)) {
                            isExists = true;
                            break;
                        }
                        if (!isContain && message.equals(s)) {
                            isExists = true;
                            break;
                        }
                    }

                }
            }
        } catch (InterruptedException ignored) {
        } finally {
            readLock.unlock();
        }
        return isExists;
    }


    public static boolean checkPrintException(String message, boolean isContain) {
        boolean isExists = false;
        if (StrUtil.isBlank(message)) {
            return isExists;
        }
        try {
            if (readLock.tryLock() || readLock.tryLock(3, TimeUnit.SECONDS)) {
                for (String s : ignorePrintException) {
                    if (StrUtil.isNotBlank(s)) {
                        if (isContain && message.contains(s)) {
                            isExists = true;
                            break;
                        }
                        if (!isContain && message.equals(s)) {
                            isExists = true;
                            break;
                        }
                    }

                }
            }
        } catch (InterruptedException ignored) {
        } finally {
            readLock.unlock();
        }
        return isExists;
    }


    // JSON 最大打印长度
    public static final Integer MAX_STRING_LENGTH = -1;

    /**
     * <p>总有些憨憨 喜欢写 System.out.println之类的语法 这里统一替换掉</p>
     * <p>支持 e.printTraceInfo()</p>
     * <p>将错误堆栈写入日志</p>
     *
     * @author bokun.li
     * @date 2023/5/27
     */
    public static void settingLog() {
        if (IS_SETTING.get()) {
            return;
        }
        IS_SETTING.set(true);
//        System.setErr(new PrintStream(System.err){
//            @Override
//            public void println(Object x) {
//                // 拦截堆栈信息 e.printStackInfo(); 因为正常来说 e.printStackInfo() 不会记录到日志文件中去
//                if(x instanceof Throwable){
//                    String message = ((Throwable) x).getMessage();
//                    if(!checkPrintException(message,true)){
//                        log.error("错误信息:{}",x);
//                    }
//                }else{
//                    log.error("错误信息:{}",x);
//                }
//                //logErrorObjToJson(x);
//            }
//        });
        System.setOut(new PrintStream(System.out) {
            @Override
            public void print(boolean b) {
                log.info(getString(String.valueOf(b)));
            }

            @Override
            public void print(char c) {
                log.info(getString(String.valueOf(c)));
            }

            @Override
            public void print(int i) {
                log.info(getString(String.valueOf(i)));
            }

            @Override
            public void print(long l) {
                log.info(getString(String.valueOf(l)));
            }

            @Override
            public void print(float f) {
                log.info(getString(String.valueOf(f)));
            }

            @Override
            public void print(double d) {
                log.info(getString(String.valueOf(d)));
            }

            @Override
            public void print(char[] s) {
                log.info(getString(String.valueOf(s)));
            }

            @Override
            public void print(String s) {
                log.info(getString(String.valueOf(s)));

            }

            @Override
            public void print(Object obj) {
                log.info(getString(String.valueOf(obj)));

            }

            @Override
            public void println() {
                log.info("");

            }

            @Override
            public void println(boolean x) {
                log.info(getString(String.valueOf(x)));

            }

            @Override
            public void println(char x) {
                log.info(getString(String.valueOf(x)));

            }

            @Override
            public void println(int x) {
                log.info(getString(String.valueOf(x)));

            }

            @Override
            public void println(long x) {
                log.info(getString(String.valueOf(x)));

            }

            @Override
            public void println(float x) {
                log.info(getString(String.valueOf(x)));

            }

            @Override
            public void println(double x) {
                log.info(getString(String.valueOf(x)));

            }

            @Override
            public void println(char[] x) {
                log.info(getString(String.valueOf(x)));

            }

            @Override
            public void println(String x) {
                log.info(x);
            }

            @Override
            public void println(Object x) {
                logObjToJson(x);
            }

        });

        log.info(SysLog.compact("已完成系统日志转换，现标准输出流也能记录到日志去"));
    }

    private static void logObjToJson(Object o) {
        log.info(getJSONorStack(o));
    }

    private static void logErrorObjToJson(Object o) {
        log.error(getJSONorStack(o));
    }

    private static String getString(String _w) {
        String w = _w;
        if (StrUtil.isNotEmpty(w)) {
            int length = w.length();
            if (MAX_STRING_LENGTH != -1 && length > MAX_STRING_LENGTH) {
                w = w.substring(0, MAX_STRING_LENGTH);
            }
        }
        return w;
    }

    /**
     * 拿取对象json字符串或者堆栈信息
     *
     * @author bokun.li
     * @date 2023/5/27
     */
    private static String getJSONorStack(Object o) {
        String w = getString(String.valueOf(o));
        if (Objects.nonNull(o)) {
            String name = o.getClass().getName();
            String name1 = Object.class.getName();
            String name2 = String.class.getName();
            if (!name1.equals(name) && !name2.equals(name) && !(o instanceof Throwable)) {
                try {
                    String s = JacksonUtil.toJson(o);
                    if (MAX_STRING_LENGTH != -1 && s.length() > MAX_STRING_LENGTH) {
                        String substring = s.substring(0, MAX_STRING_LENGTH);
                        substring += "...";
                        w = substring;
                    } else {
                        w = s;
                    }
                } catch (Exception e) {
                    // TODO
                }
            } else if (o instanceof Throwable) {
                Throwable o1 = (Throwable) o;
                w = getStackTraceInfo(o1);
            }
        }
        return w;
    }

    public static String compact(String stx, String... args) {
        if (StrUtil.isBlank(stx)) {
            return "";
        }
        String s = stx.replaceAll("\\{\\}", "%s");
        String format = String.format(s, args);
        return "【EASY4J】" + format;
    }

    public static String getStackTraceInfo(Throwable e) {
        String resultException = "";
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
        } catch (Throwable e2) {
            resultException = e2.getMessage();
        }
        if (StrUtil.isEmpty(resultException)) {
            resultException = sw.toString();
        }
        return resultException;
    }


}
