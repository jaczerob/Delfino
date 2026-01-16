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
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.manipulator.InventoryManipulator;
import dev.jaczerob.delfino.maplestory.constants.id.ItemId;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.Trade;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.server.maps.Portal;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public final class ChangeMapHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChangeMapHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHANGE_MAP;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();

        if (chr.isChangingMaps() || chr.isBanned()) {
            if (chr.isChangingMaps()) {
                log.warn("Chr {} got stuck when changing maps. Last visited mapids: {}", chr.getName(), chr.getLastVisitedMapids());
            }

            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }
        if (chr.getTrade() != null) {
            Trade.cancelTrade(chr, Trade.TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
        }

        boolean enteringMapFromCashShop = packet.available() == 0;
        if (enteringMapFromCashShop) {
            enterFromCashShop(client);
            return;
        }

        if (chr.getCashShop().isOpened()) {
            client.disconnect(false, false);
            return;
        }

        try {
            packet.readByte(); // 1 = from dying 0 = regular portals
            int targetMapId = packet.readInt();
            String portalName = packet.readString();
            Portal portal = chr.getMap().getPortal(portalName);
            packet.readByte();
            boolean wheel = packet.readByte() > 0;

            boolean chasing = packet.readByte() == 1 && chr.isGM() && packet.available() == 2 * Integer.BYTES;
            if (chasing) {
                chr.setChasing(true);
                chr.setPosition(new Point(packet.readInt(), packet.readInt()));
            }

            if (targetMapId != -1) {
                if (!chr.isAlive()) {
                    MapleMap map = chr.getMap();
                    if (wheel && chr.haveItemWithId(ItemId.WHEEL_OF_FORTUNE, false)) {
                        // thanks lucasziron (lziron) for showing revivePlayer() triggering by Wheel

                        InventoryManipulator.removeById(client, InventoryType.CASH, ItemId.WHEEL_OF_FORTUNE, 1, true, false);
                        chr.sendPacket(ChannelPacketCreator.getInstance().showWheelsLeft(chr.getItemQuantity(ItemId.WHEEL_OF_FORTUNE, false)));

                        chr.updateHp(50);
                        chr.changeMap(map, map.findClosestPlayerSpawnpoint(chr.getPosition()));
                    } else {
                        boolean executeStandardPath = true;
                        if (executeStandardPath) {
                            chr.respawn(map.getReturnMapId());
                        }
                    }
                } else {
                    if (chr.isGM()) {
                        MapleMap to = chr.getWarpMap(targetMapId);
                        chr.changeMap(to, to.getPortal(0));
                    } else {
                        final int divi = chr.getMapId() / 100;
                        boolean warp = false;
                        if (divi == 0) {
                            if (targetMapId == 10000) {
                                warp = true;
                            }
                        } else if (divi == 20100) {
                            if (targetMapId == MapId.LITH_HARBOUR) {
                                client.sendPacket(ChannelPacketCreator.getInstance().lockUI(false));
                                client.sendPacket(ChannelPacketCreator.getInstance().disableUI(false));
                                warp = true;
                            }
                        } else if (divi == 9130401) { // Only allow warp if player is already in Intro map, or else = hack
                            if (targetMapId == MapId.EREVE || targetMapId / 100 == 9130401) { // Cygnus introduction
                                warp = true;
                            }
                        } else if (divi == 9140900) { // Aran Introduction
                            if (targetMapId == MapId.ARAN_TUTO_2 || targetMapId == MapId.ARAN_TUTO_3 || targetMapId == MapId.ARAN_TUTO_4 || targetMapId == MapId.ARAN_INTRO) {
                                warp = true;
                            }
                        } else if (divi / 10 == 1020) { // Adventurer movie clip Intro
                            if (targetMapId == 1020000) {
                                warp = true;
                            }
                        } else if (divi / 10 >= 980040 && divi / 10 <= 980045) {
                            if (targetMapId == MapId.WITCH_TOWER_ENTRANCE) {
                                warp = true;
                            }
                        }
                        if (warp) {
                            final MapleMap to = chr.getWarpMap(targetMapId);
                            chr.changeMap(to, to.getPortal(0));
                        }
                    }
                }
            }

            if (portal != null && !portal.getPortalStatus()) {
                client.sendPacket(ChannelPacketCreator.getInstance().blockedMessage(1));
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                return;
            }

            if (chr.getMapId() == MapId.FITNESS_EVENT_LAST) {
                chr.getFitness().resetTimes();
            } else if (chr.getMapId() == MapId.OLA_EVENT_LAST_1 || chr.getMapId() == MapId.OLA_EVENT_LAST_2) {
                chr.getOla().resetTimes();
            }

            if (portal != null) {
                if (portal.getPosition().distanceSq(chr.getPosition()) > 400000) {
                    client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
                    return;
                }

                portal.enterPortal(client);
            } else {
                client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void enterFromCashShop(Client client) {
        final Character chr = client.getPlayer();

        if (!chr.getCashShop().isOpened()) {
            client.disconnect(false, false);
            return;
        }
        String[] socket = Server.getInstance().getInetSocket(client, client.getWorld(), client.getChannel());
        if (socket == null) {
            client.enableCSActions();
            return;
        }
        chr.getCashShop().open(false);

        chr.setSessionTransitionState();
        try {
            client.sendPacket(ChannelPacketCreator.getInstance().getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }
}