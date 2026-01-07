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
package dev.jaczerob.delfino.maplestory.net.server.channel.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.net.packet.InPacket;
import dev.jaczerob.delfino.maplestory.server.maps.Dragon;
import dev.jaczerob.delfino.maplestory.tools.PacketCreator;
import dev.jaczerob.delfino.maplestory.tools.exceptions.EmptyMovementException;

import java.awt.*;


public class MoveDragonHandler extends AbstractMovementPacketHandler {
    @Override
    public void handlePacket(InPacket p, Client c) {
        final Character chr = c.getPlayer();
        final Point startPos = new Point(p.readShort(), p.readShort());
        final Dragon dragon = chr.getDragon();
        if (dragon != null) {
            try {
                int movementDataStart = p.getPosition();
                updatePosition(p, dragon, 0);
                long movementDataLength = p.getPosition() - movementDataStart; //how many bytes were read by updatePosition
                p.seek(movementDataStart);

                if (chr.isHidden()) {
                    chr.getMap().broadcastGMPacket(chr, PacketCreator.moveDragon(dragon, startPos, p, movementDataLength));
                } else {
                    chr.getMap().broadcastMessage(chr, PacketCreator.moveDragon(dragon, startPos, p, movementDataLength), dragon.getPosition());
                }
            } catch (EmptyMovementException e) {
            }
        }
    }
}