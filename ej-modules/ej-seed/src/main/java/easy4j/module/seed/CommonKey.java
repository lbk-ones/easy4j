package easy4j.module.seed;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import easy4j.module.base.enums.DbType;
import easy4j.module.base.starter.EnvironmentHolder;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SqlFileExecute;
import easy4j.module.base.utils.SqlType;
import easy4j.module.base.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

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
public class CommonKey {

    public static final Logger log = LoggerFactory.getLogger(CommonKey.class);
    public static final String HH_IP = InetAddress.getLoopbackAddress().getHostAddress();
    private static final Snowflake snowflake;

    static {
        // 判定workerId
        long workerId = getWorkerId();
        snowflake = new Snowflake(workerId,2L);
    }


    private static long getWorkerId() {
        LinkedHashSet<String> localIpList = NetUtil.localIpv4s();
        long workerId = 0L;
        String ipSegment = EnvironmentHolder.environment.getProperty("seed.ip.segment");
        log.info(SysLog.compact("分布式雪花主键策略IP前缀为："+ipSegment));
        DataSource dataSource = SpringUtil.getBean(DataSource.class);
        JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
        jdbcTemplate.setDataSource(dataSource);

        String dbType = EnvironmentHolder.getDbType().toLowerCase();

        String classPathSqlName = "";
        if(StrUtil.equals(dbType,"h2")){
            classPathSqlName = "snowip_h2.sql";
        }else if(StrUtil.contains(dbType,"sqlserver")){
            classPathSqlName = "snowip_sqlserver.sql";
        }else if(StrUtil.equals(dbType,"postgresql")){
            classPathSqlName = "snowip_postgresql.sql";
        }else if(StrUtil.equals(dbType,"mysql")){
            classPathSqlName = "snowip_mysql.sql";
        }else if(StrUtil.equals(dbType,"oracle")){
            classPathSqlName = "snowip_oracle.sql";
        }
        if(StrUtil.isNotBlank(classPathSqlName)){
            try{
                SqlFileExecute.executeSqlFile(jdbcTemplate,classPathSqlName);
            }catch (Exception ignored){
                log.info(SysLog.compact("分布式雪花主键策略已启动"));
            }
        }
        boolean enabled = false;
        // 所有节点的 ip num 不能一样
        List<String> notHhIpList = localIpList.stream().filter(e -> !HH_IP.equals(e)).collect(Collectors.toList());
        try{
            List<WORKIP> allWorkIpList = jdbcTemplate.query("SELECT * FROM WORK_IP", BeanPropertyRowMapper.newInstance(WORKIP.class));
            ListTs.foreach(allWorkIpList, e->{if (Objects.isNull(e.getNUM())) {e.setNUM(0);}});
            // 校验是否自动开启 如果使用了 k8 那么副本ip一般都是两个 除掉环回地址肯定就是只有一个 当然也有其他情况 这里也得兼容
            boolean isEable = notHhIpList.size() == 1 || notHhIpList.stream().anyMatch(e -> StrUtil.isNotBlank(ipSegment) && e.startsWith(ipSegment));
            if(isEable){
                List<String> collect = notHhIpList.stream().filter(e -> StrUtil.isNotBlank(ipSegment) && e.startsWith(ipSegment)).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(collect) && collect.size()>=2){
                    log.info(SysLog.compact("符合分布式雪花主键策略的IP为："+ JSON.toJSONString(collect)));
                }
                String masterIp = notHhIpList.size() == 1 ? notHhIpList.get(0):ListTs.get(collect,0);
                if(Objects.nonNull(masterIp)){
                    String nextNum = allWorkIpList.isEmpty()?"1": String.valueOf(allWorkIpList.stream().max((o1, o2) -> o2.getNUM().compareTo(o1.getNUM())).get().getNUM()+1);
                    // 如果超过31了 删了重来 这个时候 极端情况下 workIp 可能会出现重复的这种情况
                    if("32".equals(nextNum)){
                        jdbcTemplate.update("DELETE FROM WORK_IP WHERE 1=1");
                        return getWorkerId();
                    }
                    workerId = Long.parseLong(nextNum);
                    if (allWorkIpList.stream().noneMatch(e->masterIp.equals(e.getIP()))) {
                        jdbcTemplate.update("INSERT INTO WORK_IP (IP,NUM) VALUES (?,?)",masterIp,nextNum);
                    }else{
                        // k8s 重启 ip是不会延续原来的 但是这里兼容一下这种情况 万一不用 k8s捏 对吧
                        String sql = String.format("SELECT * FROM WORK_IP WHERE IP = '%s'", masterIp);
                        WORKIP workip = jdbcTemplate.queryForObject(sql, BeanPropertyRowMapper.newInstance(WORKIP.class));
                        if(Objects.nonNull(workip)){
                            workerId = workip.getNUM();
                        }
                    }
                    enabled = true;
                }
            }else{
                log.warn(SysLog.compact("分布式雪花算法ip未配置，可能导致主键冲突，请配置 seed.ip.segment 【ip的前几位字母】"));
            }
        }catch (Throwable e){
            log.error("节点IP更新失败--->"+e.getMessage());
            e.printStackTrace();
        }
        // 未启动 随便搞一个
        if(!enabled){
            Random random = new Random();
            workerId = random.nextInt(31);
        }
        log.info(SysLog.compact("服务取到雪花算法的工作ID为{}", String.valueOf(workerId)));
        return workerId;
    }

    public static String getCreateTableSql(){
        String db = SqlType.getType();
        List<String> list = ListTs.asList(DbType.MYSQL.getDb(), DbType.ORACLE.getDb(),DbType.ORACLE_12C.getDb());
        if (!list.contains(db)) {
            return "";
        }

        if(DbType.MYSQL.getDb().equals(db)){
            return "CREATE TABLE WORK_IP(IP VARCHAR(100),NUM INT(20)) COMMENT '分布式主键IP记录'";
        }else{
            return "CREATE TABLE WORK_IP(IP VARCHAR2(100),NUM NUMBER(20)) COMMENT '分布式主键IP记录'";
        }
    }

    public static String gennerString(){
        return snowflake.nextIdStr();
    }

    public static long gennerLong(){
        return snowflake.nextId();
    }

    public static String leafKey(String tag){


        return "";
    }

    static class WORKIP implements Serializable {
        private String IP;
        private Integer NUM;

        public String getIP() {
            return IP;
        }

        public void setIP(String IP) {
            this.IP = IP;
        }

        public Integer getNUM() {
            return NUM;
        }

        public void setNUM(Integer NUM) {
            this.NUM = NUM;
        }
    }

}
