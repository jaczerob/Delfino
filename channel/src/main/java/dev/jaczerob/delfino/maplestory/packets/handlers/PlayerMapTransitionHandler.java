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

import dev.jaczerob.delfino.maplestory.client.BuffStat;
import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.MapObject;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author Ronan
 */
@Slf4j
@Component
public final class PlayerMapTransitionHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PLAYER_MAP_TRANSFER;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();

        final var currentMap = chr.getMap();
        if (currentMap == null) {
            MDC.put("player.map.current.id", "null");
            MDC.put("player.map.current.name", "null");
        } else {
            MDC.put("player.map.current.id", String.valueOf(currentMap.getId()));
            MDC.put("player.map.current.name", currentMap.getMapName());
        }

        log.debug("Player finished transitioning maps");

        chr.setMapTransitionComplete();

        int beaconid = chr.getBuffSource(BuffStat.HOMING_BEACON);
        if (beaconid != -1) {
            chr.cancelBuffStats(BuffStat.HOMING_BEACON);

            final List<Pair<BuffStat, Integer>> stat = Collections.singletonList(new Pair<>(BuffStat.HOMING_BEACON, 0));
            chr.sendPacket(ChannelPacketCreator.getInstance().giveBuff(1, beaconid, stat));
        }

        if (!chr.isHidden()) {  // thanks Lame (Conrad) for noticing hidden characters controlling mobs
            for (MapObject mo : chr.getMap().getMonsters()) {    // thanks BHB, IxianMace, Jefe for noticing several issues regarding mob statuses (such as freeze)
                Monster m = (Monster) mo;
                if (m.getSpawnEffect() == 0 || m.getHp() < m.getMaxHp()) {     // avoid effect-spawning mobs
                    if (m.getController() == chr) {
                        context.writeAndFlush(ChannelPacketCreator.getInstance().stopControllingMonster(m.getObjectId()));
                        m.sendDestroyData(client);
                        m.aggroRemoveController();
                    } else {
                        m.sendDestroyData(client);
                    }

                    m.sendSpawnData(client);
                    m.aggroSwitchController(chr, false);
                }
            }
        }
    }
}