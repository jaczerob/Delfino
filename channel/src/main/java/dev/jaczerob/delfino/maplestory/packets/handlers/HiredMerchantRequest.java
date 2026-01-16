package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.inventory.ItemFactory;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.server.maps.MapObjectType;
import dev.jaczerob.delfino.maplestory.server.maps.PlayerShop;
import dev.jaczerob.delfino.maplestory.server.maps.Portal;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author XoticStory
 */
@Component
public final class HiredMerchantRequest extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.HIRED_MERCHANT_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();

        try {
            for (MapObject mmo : chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapObjectType.HIRED_MERCHANT, MapObjectType.PLAYER))) {
                if (mmo instanceof Character mc) {

                    PlayerShop shop = mc.getPlayerShop();
                    if (shop != null && shop.isOwner(mc)) {
                        chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(13));
                        return;
                    }
                } else {
                    chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(13));
                    return;
                }
            }

            Point cpos = chr.getPosition();
            Portal portal = chr.getMap().findClosestTeleportPortal(cpos);
            if (portal != null && portal.getPosition().distance(cpos) < 120.0) {
                chr.sendPacket(ChannelPacketCreator.getInstance().getMiniRoomError(10));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (GameConstants.isFreeMarketRoom(chr.getMapId())) {
            if (!chr.hasMerchant()) {
                try {
                    if (ItemFactory.MERCHANT.loadItems(chr.getId(), false).isEmpty() && chr.getMerchantMeso() == 0) {
                        client.sendPacket(ChannelPacketCreator.getInstance().hiredMerchantBox());
                    } else {
                        chr.sendPacket(ChannelPacketCreator.getInstance().retrieveFirstMessage());
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
