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
import dev.jaczerob.delfino.maplestory.net.server.world.Party;
import dev.jaczerob.delfino.maplestory.net.server.world.World;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author XoticStory
 * @author BubblesDev
 * @author Ronan
 */
@Component
public class PartySearchStartHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PARTY_SEARCH_START;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int min = packet.readInt();
        int max = packet.readInt();

        Character chr = client.getPlayer();
        if (min > max) {
            chr.dropMessage(1, "The min. value is higher than the max!");
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        if (max - min > 30) {
            chr.dropMessage(1, "You can only search for party members within a range of 30 levels.");
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        if (chr.getLevel() < min || chr.getLevel() > max) {
            chr.dropMessage(1, "The range of level for search has to include your own level.");
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        packet.readInt(); // members
        int jobs = packet.readInt();

        Party party = client.getPlayer().getParty();
        if (party == null || !client.getPlayer().isPartyLeader()) {
            return;
        }

        World world = client.getWorldServer();
        world.getPartySearchCoordinator().registerPartyLeader(chr, min, max, jobs);
    }
}