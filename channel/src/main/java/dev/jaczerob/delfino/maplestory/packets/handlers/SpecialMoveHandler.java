package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.constants.skills.*;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.awt.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public final class SpecialMoveHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SPECIAL_MOVE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        packet.readInt();
        chr.getAutobanManager().setTimestamp(4, Server.getInstance().getCurrentTimestamp(), 28);
        int skillid = packet.readInt();
        
        /*
        if ((!GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(skillid)) || (!chr.isGM() && GameConstants.isGMSkills(skillid)) || (!GameConstants.isInJobTree(skillid, chr.getJob().getId()) && !chr.isGM())) {
        	AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to packet edit skills.");
        	FilePrinter.printError(FilePrinter.EXPLOITS + chr.getName() + ".txt", chr.getName() + " tried to use skill " + skillid + " without it being in their job.");
    		client.disconnect(true, false);
            return;
        }
        */

        Point pos = null;
        int __skillLevel = packet.readByte();
        Skill skill = SkillFactory.getSkill(skillid);
        int skillLevel = chr.getSkillLevel(skill);
        if (skillid % 10000000 == 1010 || skillid % 10000000 == 1011) {
            if (chr.getDojoEnergy() < 10000) { // PE hacking or maybe just lagging
                return;
            }
            skillLevel = 1;
            chr.setDojoEnergy(0);
            context.writeAndFlush(ChannelPacketCreator.getInstance().getEnergy("energy", chr.getDojoEnergy()));
            context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(5, "As you used the secret skill, your energy bar has been reset."));
        }
        if (skillLevel == 0 || skillLevel != __skillLevel) {
            return;
        }

        StatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0) {
            if (chr.skillIsCooling(skillid)) {
                return;
            } else if (skillid != Corsair.BATTLE_SHIP) {
                int cooldownTime = effect.getCooldown();
                if (StatEffect.isHerosWill(skillid) && YamlConfig.config.server.USE_FAST_REUSE_HERO_WILL) {
                    cooldownTime /= 60;
                }

                context.writeAndFlush(ChannelPacketCreator.getInstance().skillCooldown(skillid, cooldownTime));
                chr.addCooldown(skillid, currentServerTime(), SECONDS.toMillis(cooldownTime));
            }
        }
        if (skillid == Hero.MONSTER_MAGNET || skillid == Paladin.MONSTER_MAGNET || skillid == DarkKnight.MONSTER_MAGNET) { // Monster Magnet
            int num = packet.readInt();
            for (int i = 0; i < num; i++) {
                int mobOid = packet.readInt();
                byte success = packet.readByte();
                chr.getMap().broadcastMessage(chr, ChannelPacketCreator.getInstance().catchMonster(mobOid, success), false);
                Monster monster = chr.getMap().getMonsterByOid(mobOid);
                if (monster != null) {
                    if (!monster.isBoss()) {
                        monster.aggroClearDamages();
                        monster.aggroMonsterDamage(chr, 1);

                        // thanks onechord for pointing out Magnet crashing the caster (issue would actually happen upon failing to catch mob)
                        // thanks Conrad for noticing Magnet crashing when trying to pull bosses and fixed mobs
                        monster.aggroSwitchController(chr, true);
                    }
                }
            }
            byte direction = packet.readByte();   // thanks MedicOP for pointing some 3rd-party related issues with Magnet
            chr.getMap().broadcastMessage(chr, ChannelPacketCreator.getInstance().showBuffEffect(chr.getId(), skillid, chr.getSkillLevel(skillid), 1, direction), false);
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            return;
        } else if (skillid == Brawler.MP_RECOVERY) {// MP Recovery
            Skill s = SkillFactory.getSkill(skillid);
            StatEffect ef = s.getEffect(chr.getSkillLevel(s));

            int lose = chr.safeAddHP(-1 * (chr.getCurrentMaxHp() / ef.getX()));
            int gain = -lose * (ef.getY() / 100);
            chr.addMP(gain);
        } else if (skillid == SuperGM.HEAL_PLUS_DISPEL) {
            packet.skip(11);
            chr.getMap().broadcastMessage(chr, ChannelPacketCreator.getInstance().showBuffEffect(chr.getId(), skillid, chr.getSkillLevel(skillid)), false);
        } else if (skillid % 10000000 == 1004) {
            packet.readShort();
        }

        if (packet.available() == 5) {
            pos = new Point(packet.readShort(), packet.readShort());
        }
        if (chr.isAlive()) {
            if (skill.getId() != Priest.MYSTIC_DOOR) {
                if (skill.getId() % 10000000 != 1005) {
                    skill.getEffect(skillLevel).applyTo(chr, pos);
                } else {
                    skill.getEffect(skillLevel).applyEchoOfHero(chr);
                }
            } else {
                if (client.tryacquireClient()) {
                    try {
                        if (chr.canDoor()) {
                            chr.cancelMagicDoor();
                            skill.getEffect(skillLevel).applyTo(chr, pos);
                        } else {
                            chr.message("Please wait 5 seconds before casting Mystic Door again.");
                        }
                    } finally {
                        client.releaseClient();
                    }
                }

                context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
            }
        } else {
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
        }
    }
}
