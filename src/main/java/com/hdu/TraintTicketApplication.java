package com.hdu;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;
import springfox.documentation.oas.annotations.EnableOpenApi;
import com.google.common.collect.Lists;
import java.util.List;

@SpringBootApplication(exclude= {DataSourceAutoConfiguration.class})
@EnableTransactionManagement // 开启事务注解
@EnableOpenApi // http://localhost:8080/swagger-ui/index.html#/
public class TraintTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraintTicketApplication.class, args);
    }

    @Bean(name = "shardedJedisPoll")
    public ShardedJedisPool shardedJedisPool(){
		JedisShardInfo jedisShardInfo = new JedisShardInfo("127.0.0.1",6379);
//        JedisShardInfo jedisShardInfo = new JedisShardInfo(host,port);
        List<JedisShardInfo> shardInfoList = Lists.newArrayList(jedisShardInfo);
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(config,shardInfoList);
        return shardedJedisPool;
    }
}
