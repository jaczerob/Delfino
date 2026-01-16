package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.skills.*;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class SkillEffectHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(SkillEffectHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SKILL_EFFECT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int skillId = packet.readInt();
        int level = packet.readByte();
        byte flags = packet.readByte();
        int speed = packet.readByte();
        byte aids = packet.readByte();//Mmmk
        switch (skillId) {
            case FPMage.EXPLOSION:
            case FPArchMage.BIG_BANG:
            case ILArchMage.BIG_BANG:
            case Bishop.BIG_BANG:
            case Bowmaster.HURRICANE:
            case Marksman.PIERCING_ARROW:
            case ChiefBandit.CHAKRA:
            case Brawler.CORKSCREW_BLOW:
            case Gunslinger.GRENADE:
            case Corsair.RAPID_FIRE:
            case WindArcher.HURRICANE:
            case NightWalker.POISON_BOMB:
            case ThunderBreaker.CORKSCREW_BLOW:
            case Paladin.MONSTER_MAGNET:
            case DarkKnight.MONSTER_MAGNET:
            case Hero.MONSTER_MAGNET:
            case Evan.FIRE_BREATH:
            case Evan.ICE_BREATH:
                client.getPlayer().getMap().broadcastMessage(client.getPlayer(), ChannelPacketCreator.getInstance().skillEffect(client.getPlayer(), skillId, level, flags, speed, aids), false);
                return;
            default:
                log.warn("Chr {} entered SkillEffectHandler without being handled using {}", client.getPlayer(), skillId);
                return;
        }
    }
}
