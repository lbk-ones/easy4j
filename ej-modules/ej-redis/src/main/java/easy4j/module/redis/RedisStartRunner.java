package easy4j.module.redis;

import easy4j.infra.common.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;


@Slf4j
public class RedisStartRunner implements CommandLineRunner {

    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        isRedisAvailable();
    }

    public void isRedisAvailable() {
        RedisConnection connection = null;
        try {
            log.info(SysLog.compact("check redis"));
            // 获取Redis连接
            RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) {
                System.err.println("无法获取Redis连接工厂");
                return;
            }

            connection = connectionFactory.getConnection();

            // 执行PING命令
            String response = connection.ping();

            // 根据PING命令的响应判断Redis是否正常
            log.info(SysLog.compact("redis server return " + response));
            log.info(SysLog.compact("redis server reachable"));

        } catch (Exception e) {
            log.error("检测Redis服务时出错: " + e.getMessage());
        } finally {
            // 确保释放连接
            if (connection != null) {
                connection.close();
            }
        }
    }
}
