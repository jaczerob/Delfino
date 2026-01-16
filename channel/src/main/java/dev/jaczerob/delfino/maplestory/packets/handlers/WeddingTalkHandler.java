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
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.event.EventInstanceManager;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.packets.WeddingPackets;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Ronan
 */
@Component
public final class WeddingTalkHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.WEDDING_TALK;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        byte action = packet.readByte();
        if (action == 1) {
            EventInstanceManager eim = client.getPlayer().getEventInstance();

            if (eim != null && !(client.getPlayer().getId() == eim.getIntProperty("groomId") || client.getPlayer().getId() == eim.getIntProperty("brideId"))) {
                client.sendPacket(WeddingPackets.getInstance().OnWeddingProgress(false, 0, 0, (byte) 2));
            } else {
                client.sendPacket(WeddingPackets.getInstance().OnWeddingProgress(true, 0, 0, (byte) 3));
            }
        } else {
            client.sendPacket(WeddingPackets.getInstance().OnWeddingProgress(true, 0, 0, (byte) 3));
        }

        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }
}