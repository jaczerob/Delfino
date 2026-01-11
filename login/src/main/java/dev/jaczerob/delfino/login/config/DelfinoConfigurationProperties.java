package dev.jaczerob.delfino.login.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelfinoConfigurationProperties {
    private Server server = new Server();

    @Getter
    @Setter
    public static class Server {
        private int channelLoad = 100;
    }
}
