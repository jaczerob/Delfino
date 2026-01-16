package dev.jaczerob.delfino.world;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("dev.jaczerob.delfino")
@SpringBootApplication
public class WorldApplication {
    public static void main(final String... args) {
        SpringApplication.run(WorldApplication.class, args);
    }
}