package dev.jaczerob.delfino.maplestory.net.server;

import dev.jaczerob.delfino.maplestory.server.StatEffect;

/**
 * @author Danny
 */
public class PlayerBuffValueHolder {
    public int usedTime;
    public StatEffect effect;

    public PlayerBuffValueHolder(int usedTime, StatEffect effect) {
        this.usedTime = usedTime;
        this.effect = effect;
    }
}
