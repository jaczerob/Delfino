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

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.id.MobId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripting.event.EventInstanceManager;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.maplestory.tools.Randomizer;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Xotic (XoticStory) & BubblesDev
 */

@Component
public final class MobDamageMobFriendlyHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MOB_DAMAGE_MOB_FRIENDLY;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int attacker = packet.readInt();
        packet.readInt();
        int damaged = packet.readInt();

        MapleMap map = client.getPlayer().getMap();
        Monster monster = map.getMonsterByOid(damaged);

        if (monster == null || map.getMonsterByOid(attacker) == null) {
            return;
        }

        int damage = Randomizer.nextInt(((monster.getMaxHp() / 13 + monster.getPADamage() * 10)) * 2 + 500) / 10; // Formula planned by Beng.

        if (monster.getHp() - damage < 1) {     // friendly dies
            switch (monster.getId()) {
                case MobId.WATCH_HOG:
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "The Watch Hog has been injured by the aliens. Better luck next time..."));
                    break;
                case MobId.MOON_BUNNY: //moon bunny
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "The Moon Bunny went home because he was sick."));
                    break;
                case MobId.TYLUS: //tylus
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "Tylus has fallen by the overwhelming forces of the ambush."));
                    break;
                case MobId.JULIET: //juliet
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "Juliet has fainted in the middle of the combat."));
                    break;
                case MobId.ROMEO: //romeo
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "Romeo has fainted in the middle of the combat."));
                    break;
                case MobId.GIANT_SNOWMAN_LV1_EASY, MobId.GIANT_SNOWMAN_LV1_MEDIUM, MobId.GIANT_SNOWMAN_LV1_HARD:
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "The Snowman has melted on the heat of the battle."));
                    break;
                case MobId.DELLI: //delli
                    map.broadcastMessage(ChannelPacketCreator.getInstance().serverNotice(6, "Delli vanished after the ambush, sheets still laying on the ground..."));
                    break;
            }

            map.killFriendlies(monster);
        } else {
            EventInstanceManager eim = map.getEventInstance();
            if (eim != null) {
                eim.friendlyDamaged(monster);
            }
        }

        monster.applyAndGetHpDamage(damage, false);
        int remainingHp = monster.getHp();
        if (remainingHp <= 0) {
            remainingHp = 0;
            map.removeMapObject(monster);
        }

        map.broadcastMessage(ChannelPacketCreator.getInstance().MobDamageMobFriendly(monster, damage, remainingHp), monster.getPosition());
        client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
    }
}