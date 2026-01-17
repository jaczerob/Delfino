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
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.maps.HiredMerchant;
import dev.jaczerob.delfino.maplestory.server.maps.PlayerShop;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/*
 * @author Ronan
 */
@Component
public final class OwlWarpHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.OWL_WARP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int ownerid = packet.readInt();
        int mapid = packet.readInt();

        if (ownerid == client.getPlayer().getId()) {
            context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "You cannot visit your own shop."));
            return;
        }

        HiredMerchant hm = client.getWorldServer().getHiredMerchant(ownerid);   // if both hired merchant and player shop is on the same map
        PlayerShop ps;
        if (hm == null || hm.getMapId() != mapid || !hm.hasItem(client.getPlayer().getOwlSearch())) {
            ps = client.getWorldServer().getPlayerShop(ownerid);
            if (ps == null || ps.getMapId() != mapid || !ps.hasItem(client.getPlayer().getOwlSearch())) {
                if (hm == null && ps == null) {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(1));
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(3));
                }
                return;
            }

            if (ps.isOpen()) {
                if (GameConstants.isFreeMarketRoom(mapid)) {
                    if (ps.getChannel() == client.getChannel()) {
                        client.getPlayer().changeMap(mapid);

                        if (ps.isOpen()) {   //change map has a delay, must double check
                            if (!ps.visitShop(client.getPlayer())) {
                                if (!ps.isBanned(client.getPlayer().getName())) {
                                    context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(2));
                                } else {
                                    context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(17));
                                }
                            }
                        } else {
                            //context.writeAndFlush(PacketCreator.serverNotice(1, "That merchant has either been closed or is under maintenance."));
                            context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(18));
                        }
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "That shop is currently located in another channel. Current location: Channel " + hm.getChannel() + ", '" + hm.getMap().getMapName() + "'."));
                    }
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "That shop is currently located outside of the FM area. Current location: Channel " + hm.getChannel() + ", '" + hm.getMap().getMapName() + "'."));
                }
            } else {
                //context.writeAndFlush(PacketCreator.serverNotice(1, "That merchant has either been closed or is under maintenance."));
                context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(18));
            }
        } else {
            if (hm.isOpen()) {
                if (GameConstants.isFreeMarketRoom(mapid)) {
                    if (hm.getChannel() == client.getChannel()) {
                        client.getPlayer().changeMap(mapid);

                        if (hm.isOpen()) {   //change map has a delay, must double check
                            if (hm.addVisitor(client.getPlayer())) {
                                context.writeAndFlush(ChannelPacketCreator.getInstance().getHiredMerchant(client.getPlayer(), hm, false));
                                client.getPlayer().setHiredMerchant(hm);
                            } else {
                                //context.writeAndFlush(PacketCreator.serverNotice(1, hm.getOwner() + "'s merchant is full. Wait awhile before trying again."));
                                context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(2));
                            }
                        } else {
                            //context.writeAndFlush(PacketCreator.serverNotice(1, "That merchant has either been closed or is under maintenance."));
                            context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(18));
                        }
                    } else {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "That merchant is currently located in another channel. Current location: Channel " + hm.getChannel() + ", '" + hm.getMap().getMapName() + "'."));
                    }
                } else {
                    context.writeAndFlush(ChannelPacketCreator.getInstance().serverNotice(1, "That merchant is currently located outside of the FM area. Current location: Channel " + hm.getChannel() + ", '" + hm.getMap().getMapName() + "'."));
                }
            } else {
                //context.writeAndFlush(PacketCreator.serverNotice(1, "That merchant has either been closed or is under maintenance."));
                context.writeAndFlush(ChannelPacketCreator.getInstance().getOwlMessage(18));
            }
        }
    }
}