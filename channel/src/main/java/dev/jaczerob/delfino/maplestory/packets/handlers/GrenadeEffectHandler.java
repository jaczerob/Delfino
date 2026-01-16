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

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.skills.Gunslinger;
import dev.jaczerob.delfino.maplestory.constants.skills.NightWalker;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;

/*
 * @author GabrielSin
 */
@Component
public class GrenadeEffectHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(GrenadeEffectHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.GRENADE_EFFECT;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        Point position = new Point(packet.readInt(), packet.readInt());
        int keyDown = packet.readInt();
        int skillId = packet.readInt();

        switch (skillId) {
            case NightWalker.POISON_BOMB:
            case Gunslinger.GRENADE:
                int skillLevel = chr.getSkillLevel(skillId);
                if (skillLevel > 0) {
                    chr.getMap().broadcastMessage(chr, ChannelPacketCreator.getInstance().throwGrenade(chr.getId(), position, keyDown, skillId, skillLevel), position);
                }
                break;
            default:
                log.warn("The skill id: {} is not coded in {}", skillId, getClass().getSimpleName());
        }
    }

}