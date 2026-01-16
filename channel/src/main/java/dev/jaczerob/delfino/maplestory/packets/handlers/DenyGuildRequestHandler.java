package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.server.guild.Guild;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Xterminator
 */
@Component
public final class DenyGuildRequestHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.DENY_GUILD_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.readByte();
        Character cfrom = client.getWorldServer().getPlayerStorage().getCharacterByName(packet.readString());
        if (cfrom != null) {
            Guild.answerInvitation(client.getPlayer().getId(), client.getPlayer().getName(), cfrom.getGuildId(), false);
        }
    }
}
