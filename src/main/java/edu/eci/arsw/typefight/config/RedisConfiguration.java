package edu.eci.arsw.typefight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import edu.eci.arsw.typefight.model.TypeFight;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Serializable;

@Configuration
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String hostName;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.ssl}")
    private boolean ssl;
    @Value("${spring.redis.password}")
    private String accessKey;

    @Bean
    public JedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(hostName);
        factory.setPort(port);
        factory.setPassword(accessKey);
        factory.setUsePool(true);

        return factory;
    }

    @Bean
    public RedisTemplate<String, TypeFight> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TypeFight> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule().addDeserializer(TypeFight.class, new TypeFightDeserializer()));

        // Configurar el serializer para TypeFight
        Jackson2JsonRedisSerializer<TypeFight> serializer = new Jackson2JsonRedisSerializer<>(TypeFight.class);
        serializer.setObjectMapper(objectMapper);

        redisTemplate.setValueSerializer(serializer);

        return redisTemplate;
    }

    @Bean
    public JedisPool getConfig() {
        JedisPoolConfig config =new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool();
        config.setMaxTotal(100);
        config.setMaxIdle(200);
        config.setMinIdle(50);
        config.setMaxWaitMillis(30000);
        config.setTestOnBorrow(true);
        jedisPool = new JedisPool(config, hostName, port,300000,accessKey);
        return jedisPool;
    }

    @Bean(destroyMethod = "shutdown")    
    public RedissonClient redissonClient(JedisConnectionFactory jedisConnectionFactory) { 
        String url = jedisConnectionFactory.getHostName() + ":" + jedisConnectionFactory.getPort() + ",password=" + jedisConnectionFactory.getPassword() + ",ssl=true,abortConnect=false";       
        Config config = new Config();        
        config.useSingleServer()
            .setAddress("redis://" + jedisConnectionFactory.getHostName() + ":" + jedisConnectionFactory.getPort())                
            .setPassword(jedisConnectionFactory.getPassword());        
        return Redisson.create(config);    
    }

 }