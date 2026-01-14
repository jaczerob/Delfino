package dev.jaczerob.delfino.maplestory.config;

import com.esotericsoftware.yamlbeans.YamlReader;
import org.springframework.core.io.ClassPathResource;

import java.nio.file.Files;
import java.util.List;


public class YamlConfig {
    public static final String CONFIG_FILE_NAME = "config.yaml";
    public static final YamlConfig config;

    public List<WorldConfig> worlds;
    public ServerConfig server;

    static {
        final var configResource = new ClassPathResource(CONFIG_FILE_NAME);

        try (final var reader = new YamlReader(Files.newBufferedReader(configResource.getFilePath()))) {
            reader.getConfig().readConfig.setIgnoreUnknownProperties(true);
            config = reader.read(YamlConfig.class);
        } catch (final Exception exc) {
            throw new RuntimeException("Could not parse %s".formatted(CONFIG_FILE_NAME), exc);
        }
    }
}
