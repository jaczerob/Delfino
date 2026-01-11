package dev.jaczerob.delfino.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories
@EnableScheduling

@SpringBootApplication
@ComponentScan("dev.jaczerob.delfino")
public class LoginApplication {
    public static void main(final String... args) {
        SpringApplication.run(LoginApplication.class, args);
    }
}
