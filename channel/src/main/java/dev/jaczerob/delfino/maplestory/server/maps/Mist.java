package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.packets.Packet;

import java.awt.*;

public class Mist extends AbstractMapObject {
    private final Rectangle mistPosition;
    private final boolean isMobMist;
    private final int skillDelay;
    private final Character owner = null;
    private final MobSkill skill;
    private final boolean isPoisonMist;
    private final boolean isRecoveryMist;
    private Monster mob = null;
    private StatEffect source;

    public Mist(Rectangle mistPosition, Monster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.mob = mob;
        this.skill = skill;
        isMobMist = true;
        isPoisonMist = true;
        isRecoveryMist = false;
        skillDelay = 0;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return mistPosition.getLocation();
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    public Skill getSourceSkill() {
        return SkillFactory.getSkill(source.getSourceId());
    }

    public boolean isMobMist() {
        return isMobMist;
    }

    public boolean isPoisonMist() {
        return isPoisonMist;
    }

    public boolean isRecoveryMist() {
        return isRecoveryMist;
    }

    public int getSkillDelay() {
        return skillDelay;
    }

    public Monster getMobOwner() {
        return mob;
    }

    public Character getOwner() {
        return owner;
    }

    public Rectangle getBox() {
        return mistPosition;
    }

    public final Packet makeDestroyData() {
        return ChannelPacketCreator.getInstance().removeMist(getObjectId());
    }

    public final Packet makeSpawnData() {
        if (owner != null) {
            return ChannelPacketCreator.getInstance().spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId())), this);
        }
        return ChannelPacketCreator.getInstance().spawnMobMist(getObjectId(), mob.getId(), skill.getId(), this);
    }

    public final Packet makeFakeSpawnData(int level) {
        if (owner != null) {
            return ChannelPacketCreator.getInstance().spawnMist(getObjectId(), owner.getId(), getSourceSkill().getId(), level, this);
        }
        return ChannelPacketCreator.getInstance().spawnMobMist(getObjectId(), mob.getId(), skill.getId(), this);
    }

    @Override
    public void sendSpawnData(Client client) {
        client.sendPacket(makeSpawnData());
    }

    @Override
    public void sendDestroyData(Client client) {
        client.sendPacket(makeDestroyData());
    }

    public boolean makeChanceResult() {
        return source.makeChanceResult();
    }
}
