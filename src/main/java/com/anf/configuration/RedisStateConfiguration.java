package com.anf.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
    var mapper =
        JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).build();
    mapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);
    var valueSerializer = new GenericJackson2JsonRedisSerializer(mapper);
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
