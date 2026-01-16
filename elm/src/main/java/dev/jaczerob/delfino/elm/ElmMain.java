package dev.jaczerob.delfino.elm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan("dev.jaczerob.delfino")
@ComponentScan("dev.jaczerob.delfino")
@EnableRedisRepositories("dev.jaczerob.delfino")
public class ElmMain {
    public static void main(final String... args) {
        SpringApplication.run(ElmMain.class, args);
    }
}
