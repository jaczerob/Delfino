package dev.jaczerob.delfino.maplestory.net.server;

/**
 * @author Danny
 */
public class PlayerCoolDownValueHolder {
    public int skillId;
    public long startTime;
    public long length;

    public PlayerCoolDownValueHolder(int skillId, long startTime, long length) {
        this.skillId = skillId;
        this.startTime = startTime;
        this.length = length;
    }
}
