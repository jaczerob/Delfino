package dev.jaczerob.delfino.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan("dev.jaczerob.delfino")
@EnableJpaRepositories("dev.jaczerob.delfino")
@ComponentScan("dev.jaczerob.delfino")
@EnableRedisRepositories("dev.jaczerob.delfino")
public class LoginApplication {
    public static void main(final String... args) {
        SpringApplication.run(LoginApplication.class, args);
    }
}
