package dev.jaczerob.delfino.maplestory.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelfinoConfigurationProperties {
    private Server server = new Server();
    private Netty netty = new Netty();

    @Getter
    @Setter
    public static class Server {
        private short version = 83;
        private int channelLoad = 100;
    }

    @Getter
    @Setter
    public static class Netty {
        private int idleTimeSeconds = 30;
        private boolean logPackets = true;
    }
}
