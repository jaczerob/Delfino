package dev.jaczerob.delfino.maplestory.net.server.world;

import dev.jaczerob.delfino.maplestory.client.Character;

public class MessengerCharacter {
    private final String name;
    private final int id;
    private int position;
    private final int channel;
    private final boolean online;

    public MessengerCharacter(Character maplechar, int position) {
        this.name = maplechar.getName();
        this.channel = maplechar.getClient().getChannel();
        this.id = maplechar.getId();
        this.online = true;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public int getChannel() {
        return channel;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessengerCharacter other = (MessengerCharacter) obj;
        if (name == null) {
            return other.name == null;
        } else {
            return name.equals(other.name);
        }
    }
}
