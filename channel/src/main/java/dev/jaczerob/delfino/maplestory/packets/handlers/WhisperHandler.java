package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.ChatLogger;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator.WhisperFlag;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Chronos
 */
@Slf4j
@Component
public final class WhisperHandler extends AbstractPacketHandler {
    public static final byte RT_ITC = 0x00;
    public static final byte RT_SAME_CHANNEL = 0x01;
    public static final byte RT_CASH_SHOP = 0x02;
    public static final byte RT_DIFFERENT_CHANNEL = 0x03;

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.WHISPER;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        byte request = packet.readByte();
        String name = packet.readString();
        Character target = client.getWorldServer().getPlayerStorage().getCharacterByName(name);

        if (target == null) {
            context.writeAndFlush(ChannelPacketCreator.getInstance().getWhisperResult(name, false));
            return;
        }

        switch (request) {
            case WhisperFlag.LOCATION | WhisperFlag.REQUEST:
                handleFind(client.getPlayer(), target, WhisperFlag.LOCATION);
                break;
            case WhisperFlag.WHISPER | WhisperFlag.REQUEST:
                String message = packet.readString();
                handleWhisper(message, client.getPlayer(), target);
                break;
            case WhisperFlag.LOCATION_FRIEND | WhisperFlag.REQUEST:
                handleFind(client.getPlayer(), target, WhisperFlag.LOCATION_FRIEND);
                break;
            default:
                log.warn("Unknown request {} triggered by {}", request, client.getPlayer().getName());
                break;
        }
    }

    private void handleFind(Character user, Character target, byte flag) {
        if (user.gmLevel() >= target.gmLevel()) {
            if (target.getCashShop().isOpened()) {
                user.sendPacket(ChannelPacketCreator.getInstance().getFindResult(target, RT_CASH_SHOP, -1, flag));
            } else if (target.getClient().getChannel() == user.getClient().getChannel()) {
                user.sendPacket(ChannelPacketCreator.getInstance().getFindResult(target, RT_SAME_CHANNEL, target.getMapId(), flag));
            } else {
                user.sendPacket(ChannelPacketCreator.getInstance().getFindResult(target, RT_DIFFERENT_CHANNEL, target.getClient().getChannel() - 1, flag));
            }
        } else {
            // not found for whisper is the same message
            user.sendPacket(ChannelPacketCreator.getInstance().getWhisperResult(target.getName(), false));
        }
    }

    private void handleWhisper(String message, Character user, Character target) {
        if (user.getAutobanManager().getLastSpam(7) + 200 > currentServerTime()) {
            return;
        }
        user.getAutobanManager().spam(7);

        if (message.length() > Byte.MAX_VALUE) {
            AutobanFactory.PACKET_EDIT.alert(user, user.getName() + " tried to packet edit with whispers.");
            log.warn("Chr {} tried to send text with length of {}", user.getName(), message.length());
            user.getClient().disconnect(true, false);
            return;
        }

        ChatLogger.log(user.getClient(), "Whisper To " + target.getName(), message);

        target.sendPacket(ChannelPacketCreator.getInstance().getWhisperReceive(user.getName(), user.getClient().getChannel() - 1, user.isGM(), message));

        boolean hidden = target.isHidden() && target.gmLevel() > user.gmLevel();
        user.sendPacket(ChannelPacketCreator.getInstance().getWhisperResult(target.getName(), !hidden));
    }
}
