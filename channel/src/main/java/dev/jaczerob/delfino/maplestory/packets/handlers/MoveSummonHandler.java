package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.server.maps.Summon;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.exceptions.EmptyMovementException;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Collection;

@Component
public final class MoveSummonHandler extends AbstractMovementPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MOVE_SUMMON;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int oid = packet.readInt();
        Point startPos = new Point(packet.readShort(), packet.readShort());
        Character player = client.getPlayer();
        Collection<Summon> summons = player.getSummonsValues();
        Summon summon = null;
        for (Summon sum : summons) {
            if (sum.getObjectId() == oid) {
                summon = sum;
                break;
            }
        }
        if (summon != null) {
            try {
                int movementDataStart = packet.getPosition();
                updatePosition(packet, summon, 0);
                long movementDataLength = packet.getPosition() - movementDataStart; //how many bytes were read by updatePosition
                packet.seek(movementDataStart);

                player.getMap().broadcastMessage(player, ChannelPacketCreator.getInstance().moveSummon(player.getId(), oid, startPos, packet, movementDataLength), summon.getPosition());
            } catch (EmptyMovementException e) {
            }
        }
    }
}
