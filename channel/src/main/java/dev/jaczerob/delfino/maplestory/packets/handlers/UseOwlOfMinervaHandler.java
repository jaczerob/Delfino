/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Ronan
 */
@Component
public final class UseOwlOfMinervaHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.OWL_ACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        List<Pair<Integer, Integer>> owlSearched = client.getWorldServer().getOwlSearchedItems();
        List<Integer> owlLeaderboards;

        if (owlSearched.size() < 5) {
            owlLeaderboards = new LinkedList<>();
            for (int itemId : ItemId.getOwlItems()) {
                owlLeaderboards.add(itemId);
            }
        } else {
            // descending order
            Comparator<Pair<Integer, Integer>> comparator = (p1, p2) -> p2.getRight().compareTo(p1.getRight());

            PriorityQueue<Pair<Integer, Integer>> queue = new PriorityQueue<>(Math.max(1, owlSearched.size()), comparator);
            queue.addAll(owlSearched);

            owlLeaderboards = new LinkedList<>();
            for (int i = 0; i < Math.min(owlSearched.size(), 10); i++) {
                owlLeaderboards.add(queue.remove().getLeft());
            }
        }

        client.sendPacket(ChannelPacketCreator.getInstance().getOwlOpen(owlLeaderboards));
    }
}