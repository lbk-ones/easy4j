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
package easy4j.module.seed;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import easy4j.module.base.plugin.seed.Easy4jSeed;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SysLog;
import easy4j.module.base.utils.json.JacksonUtil;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 主键生成策略 当一个副本的节点超过31台之后就不好使了这个 就得采用其他高级货了
 *
 * @author bokun.li
 * @date 2023/11/19
 */
public class CommonKey implements Easy4jSeed {

    public static final Logger log = LoggerFactory.getLogger(CommonKey.class);
    public static final String HH_IP = InetAddress.getLoopbackAddress().getHostAddress();
    private static final Snowflake snowflake;

    static {
        // 判定workerId
        long workerId = getWorkerId();
        snowflake = new Snowflake(workerId, 2L);
    }

    private CommonKey() {

    }

    private static class CommonKeyHolder {
        private static final CommonKey INSTANCE = new CommonKey();
    }

    public static CommonKey getCommonKey() {
        return CommonKeyHolder.INSTANCE;
    }


    private static long getWorkerId() {
        LinkedHashSet<String> localIpList = NetUtil.localIpv4s();
        long workerId = 0L;
        String ipSegment = Easy4j.getEjSysProperties().getSeedIpSegment();
        log.info(SysLog.compact("分布式雪花主键策略IP前缀为：" + ipSegment));

        DBAccess dbAccess = DBAccessFactory.getDBAccess(SpringUtil.getBean(DataSource.class));

        boolean enabled = false;
        // 所有节点的 ip num 不能一样
        List<String> notHhIpList = localIpList.stream().filter(e -> !HH_IP.equals(e)).collect(Collectors.toList());
        try {
            List<SYS_WORK_IP> allWorkIpList = dbAccess.selectAll(SYS_WORK_IP.class);
            //List<SYS_WORK_IP> allWorkIpList = jdbcTemplate.query("SELECT * FROM SYS_WORK_IP", BeanPropertyRowMapper.newInstance(SYS_WORK_IP.class));
            ListTs.foreach(allWorkIpList, e -> {
                if (Objects.isNull(e.getNUM())) {
                    e.setNUM(0);
                }
            });
            // 校验是否自动开启 如果使用了 k8 那么副本ip一般都是两个 除掉环回地址肯定就是只有一个 当然也有其他情况 这里也得兼容
            boolean isEable = notHhIpList.size() == 1 || notHhIpList.stream().anyMatch(e -> StrUtil.isNotBlank(ipSegment) && e.startsWith(ipSegment));
            if (isEable) {
                List<String> collect = notHhIpList.stream().filter(e -> StrUtil.isNotBlank(ipSegment) && e.startsWith(ipSegment)).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(collect) && collect.size() >= 2) {
                    log.info(SysLog.compact("符合分布式雪花主键策略的IP为：" + JacksonUtil.toJson(collect)));
                }
                String masterIp = notHhIpList.size() == 1 ? notHhIpList.get(0) : ListTs.get(collect, 0);
                if (Objects.nonNull(masterIp)) {
                    String nextNum = allWorkIpList.isEmpty() ? "1" : String.valueOf(allWorkIpList.stream().max((o1, o2) -> o2.getNUM().compareTo(o1.getNUM())).get().getNUM() + 1);
                    // 如果超过31了 删了重来 这个时候 极端情况下 workIp 可能会出现重复的这种情况
                    if ("32".equals(nextNum)) {

                        dbAccess.deleteAll(SYS_WORK_IP.class);

                        //jdbcTemplate.update("DELETE FROM SYS_WORK_IP WHERE 1=1");
                        return getWorkerId();
                    }
                    workerId = Long.parseLong(nextNum);
                    if (allWorkIpList.stream().noneMatch(e -> masterIp.equals(e.getIP()))) {
                        SYS_WORK_IP workIp = new SYS_WORK_IP();
                        workIp.setIP(masterIp);
                        workIp.setNUM(Integer.parseInt(nextNum));
                        int i = dbAccess.saveOne(workIp, SYS_WORK_IP.class);
                        //jdbcTemplate.update("INSERT INTO SYS_WORK_IP (IP,NUM) VALUES (?,?)", masterIp, nextNum);
                    } else {
                        // k8s 重启 ip是不会延续原来的 但是这里兼容一下这种情况 万一不用 k8s捏 对吧
                        SYS_WORK_IP workip = dbAccess.selectByPrimaryKey(masterIp, SYS_WORK_IP.class);
                        if (Objects.nonNull(workip)) {
                            workerId = workip.getNUM();
                        }
                    }
                    enabled = true;
                }
            } else {
                log.warn(SysLog.compact("分布式雪花算法ip未配置，可能导致主键冲突，请配置 seed.ip.segment 【ip的前几位字母】"));
            }
        } catch (Throwable e) {
            log.error("节点IP更新失败--->" + e.getMessage());
            e.printStackTrace();
        }
        // 未启动 随便搞一个
        if (!enabled) {
            Random random = new Random();
            workerId = random.nextInt(31);
        }
        log.info(SysLog.compact("服务取到雪花算法的工作ID为{}", String.valueOf(workerId)));
        return workerId;
    }


    public static String gennerString() {
        return snowflake.nextIdStr();
    }

    public static long gennerLong() {
        return snowflake.nextId();
    }

    public static String leafKey(String tag) {


        return "";
    }

    @Setter
    @Getter
    @JdbcTable(name = "SYS_WORK_IP")
    static class SYS_WORK_IP implements Serializable {

        @JdbcColumn(name = "ip", isPrimaryKey = true)
        private String IP;
        private Integer NUM;

    }

    @Override
    public String nextIdStr() {
        return gennerString();
    }

    @Override
    public long nextIdLong() {
        return gennerLong();
    }
}
