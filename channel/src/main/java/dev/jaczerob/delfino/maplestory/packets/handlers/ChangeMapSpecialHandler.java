package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.Trade;
import dev.jaczerob.delfino.maplestory.server.Trade.TradeResult;
import dev.jaczerob.delfino.maplestory.server.maps.Portal;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class ChangeMapSpecialHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHANGE_MAP_SPECIAL;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        packet.readByte();
        String startwp = packet.readString();
        packet.readShort();
        Portal portal = client.getPlayer().getMap().getPortal(startwp);
        if (portal == null || client.getPlayer().portalDelay() > currentServerTime() || client.getPlayer().getBlockedPortals().contains(portal.getScriptName())) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }
        if (client.getPlayer().isChangingMaps() || client.getPlayer().isBanned()) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }
        if (client.getPlayer().getTrade() != null) {
            Trade.cancelTrade(client.getPlayer(), TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
        }
        portal.enterPortal(client);
    }
}
