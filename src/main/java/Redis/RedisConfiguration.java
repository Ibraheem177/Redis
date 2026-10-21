package Redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

@Configuration
public class RedisConfiguration {

    @Bean(destroyMethod = "close")
    RedisClient redisClient(@Value("${redis.host}") String host,
                            @Value("${redis.port}") int port) {
        return RedisClient.create(host, port);
    }
}
