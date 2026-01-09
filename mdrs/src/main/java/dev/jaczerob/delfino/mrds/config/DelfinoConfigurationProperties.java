package dev.jaczerob.delfino.mrds.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DelfinoConfigurationProperties {
    private World world = new World();

    @Getter
    @Setter
    public static class World {
        private int id = 0;
        private String name = "Delfino";
        private int flag = 0;
        private Messages messages = new Messages();
        private Rates rates = new Rates();
        private List<Channel> channels = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Messages {
        private String event = "Welcome!";
        private String server = "Welcome!";
        private String recommended = "Welcome!";
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
    public static class Channel {
        private int id = 0;
        private int capacity = 1000;
    }
}
