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
import dev.jaczerob.delfino.maplestory.client.inventory.ItemFactory;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.net.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.net.packet.InPacket;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapObjectType;
import dev.jaczerob.delfino.maplestory.server.maps.PlayerShop;
import dev.jaczerob.delfino.maplestory.server.maps.Portal;
import dev.jaczerob.delfino.maplestory.tools.PacketCreator;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author XoticStory
 */
public class HiredMerchantRequest extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();

        try {
            for (MapObject mmo : chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapObjectType.HIRED_MERCHANT, MapObjectType.PLAYER))) {
                if (mmo instanceof Character mc) {

                    PlayerShop shop = mc.getPlayerShop();
                    if (shop != null && shop.isOwner(mc)) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(13));
                        return;
                    }
                } else {
                    chr.sendPacket(PacketCreator.getMiniRoomError(13));
                    return;
                }
            }

            Point cpos = chr.getPosition();
            Portal portal = chr.getMap().findClosestTeleportPortal(cpos);
            if (portal != null && portal.getPosition().distance(cpos) < 120.0) {
                chr.sendPacket(PacketCreator.getMiniRoomError(10));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (GameConstants.isFreeMarketRoom(chr.getMapId())) {
            if (!chr.hasMerchant()) {
                try {
                    if (ItemFactory.MERCHANT.loadItems(chr.getId(), false).isEmpty() && chr.getMerchantMeso() == 0) {
                        c.sendPacket(PacketCreator.hiredMerchantBox());
                    } else {
                        chr.sendPacket(PacketCreator.retrieveFirstMessage());
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                chr.dropMessage(1, "You already have a store open.");
            }
        } else {
            chr.dropMessage(1, "You cannot open your hired merchant here.");
        }
    }
}
