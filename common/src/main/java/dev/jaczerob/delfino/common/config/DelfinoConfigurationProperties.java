package dev.jaczerob.delfino.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DelfinoConfigurationProperties {
    private Server server = new Server();
    private Netty netty = new Netty();
    private Mdrs mdrs = new Mdrs();
    private World world = new World();

    @Getter
    @Setter
    public static class World {
        private String name = "";
        private int id = 1;
        private int flag = 0;
        private String eventMessage = "Welcome!";
        private List<Channel> channels = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Channel {
        private int id = 1;
        private int capacity = 0;
    }

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
