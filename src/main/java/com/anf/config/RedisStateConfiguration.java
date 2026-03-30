package com.anf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisStateConfiguration {

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    var redisTemplate = new RedisTemplate<String, Object>();
    var stringSerializer = new StringRedisSerializer();
    var valueSerializer = new GenericJackson2JsonRedisSerializer();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(stringSerializer);
    redisTemplate.setHashKeySerializer(stringSerializer);
    redisTemplate.setValueSerializer(valueSerializer);
    redisTemplate.setHashValueSerializer(valueSerializer);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  @Bean("protobufRedisTemplate")
  public RedisTemplate<byte[], byte[]> protobufRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    var redisTemplate = new RedisTemplate<byte[], byte[]>();
    var byteArraySerializer = RedisSerializer.byteArray();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(byteArraySerializer);
    redisTemplate.setHashKeySerializer(byteArraySerializer);
    redisTemplate.setValueSerializer(byteArraySerializer);
    redisTemplate.setHashValueSerializer(byteArraySerializer);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }
}
