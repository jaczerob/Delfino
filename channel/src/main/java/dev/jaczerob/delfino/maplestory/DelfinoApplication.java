package dev.jaczerob.delfino.maplestory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("dev.jaczerob.delfino")
@EnableJpaRepositories("dev.jaczerob.delfino")
@ComponentScan("dev.jaczerob.delfino")
@EnableScheduling
public class DelfinoApplication {
    public static void main(final String... args) {
        SpringApplication.run(DelfinoApplication.class, args);
    }
}
