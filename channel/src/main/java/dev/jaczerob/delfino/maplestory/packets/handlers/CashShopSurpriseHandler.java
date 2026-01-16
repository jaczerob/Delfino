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
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.CashShop;
import dev.jaczerob.delfino.maplestory.server.CashShop.CashShopSurpriseResult;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author RonanLana
 * @author Ponk
 */
@Component
public class CashShopSurpriseHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CASHSHOP_OPERATION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        CashShop cs = client.getPlayer().getCashShop();
        if (!cs.isOpened()) {
            return;
        }

        long cashId = packet.readLong();
        Optional<CashShopSurpriseResult> result = cs.openCashShopSurprise(cashId);
        if (result.isEmpty()) {
            client.sendPacket(ChannelPacketCreator.getInstance().onCashItemGachaponOpenFailed());
            return;
        }

        Item usedCashShopSurprise = result.get().usedCashShopSurprise();
        Item reward = result.get().reward();
        client.sendPacket(ChannelPacketCreator.getInstance().onCashGachaponOpenSuccess(client.getAccID(), usedCashShopSurprise.getCashId(),
                usedCashShopSurprise.getQuantity(), reward, reward.getItemId(), reward.getQuantity(), true));
    }
}
