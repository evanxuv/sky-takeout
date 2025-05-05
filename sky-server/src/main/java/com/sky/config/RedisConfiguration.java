package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author evan
 * @version 1.0
 */
@Slf4j
@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        log.info("初始化redisTemplate。。。");
        // 设置key的序列化器StringRedisSerializer , 默认是 JdkSerializationRedisSerializer
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // 不推荐修改value的序列化器,修改后不会进行自动类型转换
        // redisTemplate.setValueSerializer(new StringRedisSerializer());
        // 通过redis工厂创建对象
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;



    }
}
