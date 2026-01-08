package dev.jaczerob.delfino.login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories
@SpringBootApplication
public class LoginApplication {
    public static void main(final String... args) {
        SpringApplication.run(LoginApplication.class, args);
    }
}
