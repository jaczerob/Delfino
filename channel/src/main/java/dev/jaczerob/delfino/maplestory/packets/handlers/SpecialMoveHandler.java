package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
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
            skillLevel = 1;
            context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(5, "As you used the secret skill, your energy bar has been reset."));
        }
        if (skillLevel == 0 || skillLevel != __skillLevel) {
            return;
        }

        StatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0) {
            if (chr.skillIsCooling(skillid)) {
                return;
            } else {
                int cooldownTime = effect.getCooldown();

                context.writeAndFlush(ChannelPacketCreator.getInstance().skillCooldown(skillid, cooldownTime));
                chr.addCooldown(skillid, currentServerTime(), SECONDS.toMillis(cooldownTime));
            }
        }
        if (skillid % 10000000 == 1004) {
            packet.readShort();
        }

        if (packet.available() == 5) {
            pos = new Point(packet.readShort(), packet.readShort());
        }
        if (chr.isAlive()) {
            if (skill.getId() % 10000000 != 1005) {
                skill.getEffect(skillLevel).applyTo(chr, pos);
            } else {
                skill.getEffect(skillLevel).applyEchoOfHero(chr);
            }
        } else {
            context.writeAndFlush(ChannelPacketCreator.getInstance().enableActions());
        }
    }
}
