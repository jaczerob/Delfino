package dev.jaczerob.delfino.maplestory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class DelfinoApplication {
    public static void main(final String... args) {
        SpringApplication.run(DelfinoApplication.class, args);
    }
}
