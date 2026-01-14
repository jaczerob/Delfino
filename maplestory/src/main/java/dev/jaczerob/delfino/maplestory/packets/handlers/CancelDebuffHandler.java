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

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class CancelDebuffHandler extends AbstractPacketHandler {//TIP: BAD STUFF LOL!

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CANCEL_DEBUFF;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        /*List<Disease> diseases = client.getPlayer().getDiseases();
         List<Disease> diseases_ = new ArrayList<Disease>();
         for (Disease disease : diseases) {
         List<Disease> disease_ = new ArrayList<Disease>();
         disease_.add(disease);
         diseases_.add(disease);
         client.sendPacket(PacketCreator.cancelDebuff(disease_));
         client.getPlayer().getMap().broadcastMessage(client.getPlayer(), PacketCreator.cancelForeignDebuff(client.getPlayer().getId(), disease_), false);
         }
         for (Disease disease : diseases_) {
         client.getPlayer().removeDisease(disease);
         }*/
    }
}