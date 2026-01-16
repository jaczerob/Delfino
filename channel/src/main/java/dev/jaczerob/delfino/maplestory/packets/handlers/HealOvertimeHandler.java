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
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanManager;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class HealOvertimeHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.HEAL_OVER_TIME;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        if (!chr.isLoggedinWorld()) {
            return;
        }

        AutobanManager abm = chr.getAutobanManager();
        int timestamp = Server.getInstance().getCurrentTimestamp();
        packet.skip(8);

        short healHP = packet.readShort();
        if (healHP != 0) {
            abm.setTimestamp(8, timestamp, 28);  // thanks Vcoc & Thora for pointing out d/client happening here
            if ((abm.getLastSpam(0) + 1500) > timestamp) {
                AutobanFactory.FAST_HP_HEALING.addPoint(abm, "Fast hp healing");
            }

            MapleMap map = chr.getMap();
            int abHeal = (int) (77 * map.getRecovery() * 1.5); // thanks Ari for noticing players not getting healed in sauna in certain cases
            if (healHP > abHeal) {
                AutobanFactory.HIGH_HP_HEALING.autoban(chr, "Healing: " + healHP + "; Max is " + abHeal + ".");
                return;
            }

            chr.addHP(healHP);
            chr.getMap().broadcastMessage(chr, ChannelPacketCreator.getInstance().showHpHealed(chr.getId(), healHP), false);
            abm.spam(0, timestamp);
        }
        short healMP = packet.readShort();
        if (healMP != 0 && healMP < 1000) {
            abm.setTimestamp(9, timestamp, 28);
            if ((abm.getLastSpam(1) + 1500) > timestamp) {
                AutobanFactory.FAST_MP_HEALING.addPoint(abm, "Fast mp healing");
                return;     // thanks resinate for noticing mp being gained even after detection
            }
            chr.addMP(healMP);
            abm.spam(1, timestamp);
        }
    }
}
