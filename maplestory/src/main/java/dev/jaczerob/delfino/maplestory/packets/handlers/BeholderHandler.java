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
import dev.jaczerob.delfino.maplestory.constants.skills.DarkKnight;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.Summon;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author BubblesDev
 */
@Component
public final class BeholderHandler extends AbstractPacketHandler {//Summon Skills noobs

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.BEHOLDER;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        //System.out.println(slea.toString());
        Collection<Summon> summons = client.getPlayer().getSummonsValues();
        int oid = packet.readInt();
        Summon summon = null;
        for (Summon sum : summons) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }
        if (summon != null) {
            int skillId = packet.readInt();
            if (skillId == DarkKnight.AURA_OF_BEHOLDER) {
                packet.readShort(); //Not sure.
            } else if (skillId == DarkKnight.HEX_OF_BEHOLDER) {
                packet.readByte(); //Not sure.
            }            //show to others here
        } else {
            client.getPlayer().clearSummons();
        }
    }
}
