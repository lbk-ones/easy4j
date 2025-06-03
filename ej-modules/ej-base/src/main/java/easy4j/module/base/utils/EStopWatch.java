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

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

/**
 * EStopWatch
 * 改良版步进器
 *
 * @author bokun.li
 * @date 2025-06-03 21:32:33
 */
@Slf4j
public class EStopWatch {
    private final LinkedList<Long> timestamps = new LinkedList<>(); // 存储时间戳（毫秒）
    private final LinkedList<String> stepNames = new LinkedList<>(); // 存储阶段名称

    private String taskName;

    public EStopWatch(String taskName) {
        this.taskName = taskName;
    }

    private final Object lock = new Object();


    /**
     * 开始计时（自动记录起始时间）
     */
    public void startTask(String taskName_) {
        this.taskName = taskName_;
        timestamps.clear();
        stepNames.clear();
        timestamps.add(System.currentTimeMillis()); // 记录起始时间
    }

    /**
     * 记录一个阶段的结束，计算与上一阶段的时间间隔
     *
     * @param stepName 阶段名称（如："数据库查询"）
     * @return 该阶段的耗时（毫秒）
     */
    public long step(String stepName) {
        try {
            if (timestamps.isEmpty()) {
                return 0L;
            }
            synchronized (lock) {
                long currentTime = System.currentTimeMillis();
                long lastTime = timestamps.getLast();
                long duration = currentTime - lastTime;

                // 记录阶段名称和结束时间
                stepNames.add(stepName);
                timestamps.add(currentTime);
                return duration;
            }
        } catch (Exception ignored) {

        }
        return 0L;

    }

    /**
     * 获取所有阶段的耗时日志
     *
     * @return 阶段耗时列表（格式："阶段名称: 耗时 ms"）
     */
    public List<String> getLog() {
        List<String> log = new LinkedList<>();
        for (int i = 1; i < timestamps.size(); i++) {
            String name = stepNames.get(i - 1);
            long start = timestamps.get(i - 1);
            long end = timestamps.get(i);
            log.add(name + ": " + (end - start) + " ms");
        }
        return log;
    }

    public void prettyPrint() {
        String sb = "[" + taskName + "]计时日志（总耗时：" + getTotalTime() + " ms）";
        log.info(sb);
        getLog().forEach(step -> {
            log.info("- " + step);
        });
    }

    /**
     * 获取总耗时（从 start() 到最后一次 step() 的时间）
     *
     * @return 总耗时（毫秒）
     */
    public long getTotalTime() {
        if (timestamps.isEmpty()) {
            return 0;
        }
        return System.currentTimeMillis() - timestamps.getFirst();
    }
}