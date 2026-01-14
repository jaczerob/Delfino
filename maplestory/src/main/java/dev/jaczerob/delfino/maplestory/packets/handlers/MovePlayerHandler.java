package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.exceptions.EmptyMovementException;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class MovePlayerHandler extends AbstractMovementPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MOVE_PLAYER;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.skip(9);
        try {   // thanks Sa for noticing empty movement sequences crashing players
            int movementDataStart = packet.getPosition();
            updatePosition(packet, client.getPlayer(), 0);
            long movementDataLength = packet.getPosition() - movementDataStart; //how many bytes were read by updatePosition
            packet.seek(movementDataStart);

            client.getPlayer().getMap().movePlayer(client.getPlayer(), client.getPlayer().getPosition());
            if (client.getPlayer().isHidden()) {
                client.getPlayer().getMap().broadcastGMMessage(client.getPlayer(), ChannelPacketCreator.getInstance().movePlayer(client.getPlayer().getId(), packet, movementDataLength), false);
            } else {
                client.getPlayer().getMap().broadcastMessage(client.getPlayer(), ChannelPacketCreator.getInstance().movePlayer(client.getPlayer().getId(), packet, movementDataLength), false);
            }
        } catch (EmptyMovementException e) {
        }
    }
}
