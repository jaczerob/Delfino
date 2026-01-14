package dev.jaczerob.delfino.maplestory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelfinoConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "delfino")
    public DelfinoConfigurationProperties delfinoConfigurationProperties() {
        return new DelfinoConfigurationProperties();
    }
}
