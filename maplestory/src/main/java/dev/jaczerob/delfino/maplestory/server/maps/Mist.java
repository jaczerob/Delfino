package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.constants.skills.*;
import dev.jaczerob.delfino.network.packets.Packet;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;

import java.awt.*;

/**
 * @author LaiLaiNoob
 */
public class Mist extends AbstractMapObject {
    private final Rectangle mistPosition;
    private Character owner = null;
    private Monster mob = null;
    private StatEffect source;
    private MobSkill skill;
    private final boolean isMobMist;
    private boolean isPoisonMist;
    private boolean isRecoveryMist;
    private final int skillDelay;

    public Mist(Rectangle mistPosition, Monster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.mob = mob;
        this.skill = skill;
        isMobMist = true;
        isPoisonMist = true;
        isRecoveryMist = false;
        skillDelay = 0;
    }

    public Mist(Rectangle mistPosition, Character owner, StatEffect source) {
        this.mistPosition = mistPosition;
        this.owner = owner;
        this.source = source;
        this.skillDelay = 8;
        this.isMobMist = false;
        this.isRecoveryMist = false;
        this.isPoisonMist = false;
        switch (source.getSourceId()) {
            case Evan.RECOVERY_AURA:
                isRecoveryMist = true;
                break;

            case Shadower.SMOKE_SCREEN: // Smoke Screen
                isPoisonMist = false;
                break;

            case FPMage.POISON_MIST: // FP mist
            case BlazeWizard.FLAME_GEAR: // Flame Gear
            case NightWalker.POISON_BOMB: // Poison Bomb
                isPoisonMist = true;
                break;
        }
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return mistPosition.getLocation();
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

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
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
