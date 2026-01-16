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
    private Cache cache = new Cache();

    @Getter
    @Setter
    public static class Cache {
        private boolean login = false;
    }

    @Getter
    @Setter
    public static class Rates {
        private int exp = 1;
        private int meso = 1;
        private int drop = 1;
        private int bossDrop = 1;
        private int quest = 1;
        private int fishing = 1;
        private int travel = 1;
    }

    @Getter
    @Setter
    public static class Messages {
        private String event = "Welcome!";
        private String recommended = "Welcome!";
        private String server = "Welcome!";
    }

    @Getter
    @Setter
    public static class World {
        private String url = "";
        private String name = "";
        private int id = 1;
        private int flag = 0;
        private Messages messages = new Messages();
        private Rates rates = new Rates();
        private List<Channel> channels = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Channel {
        private int port = 7575;
        private String ip = "127.0.0.1";
        private int capacity = 1000;
        private boolean enabled = true;
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
