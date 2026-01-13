package dev.jaczerob.delfino.login.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DelfinoConfigurationProperties {
    private Server server = new Server();
    private Netty netty = new Netty();
    private Mdrs mdrs = new Mdrs();

    @Getter
    @Setter
    public static class Mdrs {
        private String url = "";
    }

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
