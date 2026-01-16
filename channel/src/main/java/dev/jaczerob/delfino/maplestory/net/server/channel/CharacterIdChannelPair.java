package dev.jaczerob.delfino.maplestory.net.server.channel;

/**
 * @author Frz
 */
public class CharacterIdChannelPair {
    private int charid;
    private int channel;

    public CharacterIdChannelPair() {
    }

    public CharacterIdChannelPair(int charid, int channel) {
        this.charid = charid;
        this.channel = channel;
    }

    public int getCharacterId() {
        return charid;
    }

    public int getChannel() {
        return channel;
    }
}
