/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

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
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MiniDungeonInfo;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Flav
 */
@Component
public class EnterCashShopHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ENTER_CASHSHOP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        try {
            Character mc = client.getPlayer();

            if (mc.cannotEnterCashShop()) {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            if (MiniDungeonInfo.isDungeonMap(mc.getMapId())) {
                client.sendPacket(ChannelPacketCreator.getInstance().serverNotice(5, "Changing channels or entering Cash Shop or MTS are disabled when inside a Mini-Dungeon."));
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            if (mc.getCashShop().isOpened()) {
                return;
            }

            mc.closePlayerInteractions();
            mc.closePartySearchInteractions();

            mc.unregisterChairBuff();
            Server.getInstance().getPlayerBuffStorage().addBuffsToStorage(mc.getId(), mc.getAllBuffs());
            Server.getInstance().getPlayerBuffStorage().addDiseasesToStorage(mc.getId(), mc.getAllDiseases());
            mc.setAwayFromChannelWorld();
            mc.notifyMapTransferToPartner(-1);
            mc.removeIncomingInvites();
            mc.cancelAllBuffs(true);
            mc.cancelAllDebuffs();
            mc.cancelBuffExpireTask();
            mc.cancelDiseaseExpireTask();
            mc.cancelSkillCooldownTask();
            mc.cancelExpirationTask();

            mc.forfeitExpirableQuests();
            mc.cancelQuestExpirationTask();

            client.sendPacket(ChannelPacketCreator.getInstance().openCashShop(client, false));
            client.sendPacket(ChannelPacketCreator.getInstance().showCashInventory(client));
            client.sendPacket(ChannelPacketCreator.getInstance().showGifts(mc.getCashShop().loadGifts()));
            client.sendPacket(ChannelPacketCreator.getInstance().showWishList(mc, false));
            client.sendPacket(ChannelPacketCreator.getInstance().showCash(mc));

            client.getChannelServer().removePlayer(mc);
            mc.getMap().removePlayer(mc);
            mc.getCashShop().open(true);
            mc.saveCharToDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
