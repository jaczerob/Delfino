package dev.jaczerob.delfino.login.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class DelfinoConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "delfino")
    public DelfinoConfigurationProperties delfinoConfigurationProperties() {
        return new DelfinoConfigurationProperties();
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final JedisConnectionFactory jedisConnectionFactory) {
        final var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory);
        return template;
    }
}
