/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.server.maps.Dragon;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.exceptions.EmptyMovementException;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.awt.*;


@Component
public class MoveDragonHandler extends AbstractMovementPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MOVE_DRAGON;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        final Character chr = client.getPlayer();
        final Point startPos = new Point(packet.readShort(), packet.readShort());
        final Dragon dragon = chr.getDragon();
        if (dragon != null) {
            try {
                int movementDataStart = packet.getPosition();
                updatePosition(packet, dragon, 0);
                long movementDataLength = packet.getPosition() - movementDataStart; //how many bytes were read by updatePosition
                packet.seek(movementDataStart);

                if (chr.isHidden()) {
                    chr.getMap().broadcastGMPacket(chr, ChannelPacketCreator.getInstance().moveDragon(dragon, startPos, packet, movementDataLength));
                } else {
                    chr.getMap().broadcastMessage(chr, ChannelPacketCreator.getInstance().moveDragon(dragon, startPos, packet, movementDataLength), dragon.getPosition());
                }
            } catch (EmptyMovementException e) {
            }
        }
    }
}