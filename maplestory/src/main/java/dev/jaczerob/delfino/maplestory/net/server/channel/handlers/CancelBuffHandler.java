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
package dev.jaczerob.delfino.maplestory.net.server.channel.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.constants.skills.Bishop;
import dev.jaczerob.delfino.maplestory.constants.skills.Bowmaster;
import dev.jaczerob.delfino.maplestory.constants.skills.Corsair;
import dev.jaczerob.delfino.maplestory.constants.skills.Evan;
import dev.jaczerob.delfino.maplestory.constants.skills.FPArchMage;
import dev.jaczerob.delfino.maplestory.constants.skills.ILArchMage;
import dev.jaczerob.delfino.maplestory.constants.skills.Marksman;
import dev.jaczerob.delfino.maplestory.constants.skills.WindArcher;
import dev.jaczerob.delfino.maplestory.net.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.net.PacketHandler;
import dev.jaczerob.delfino.maplestory.net.packet.InPacket;
import dev.jaczerob.delfino.maplestory.tools.PacketCreator;

public final class CancelBuffHandler extends AbstractPacketHandler implements PacketHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        int sourceid = p.readInt();

        switch (sourceid) {
            case FPArchMage.BIG_BANG:
            case ILArchMage.BIG_BANG:
            case Bishop.BIG_BANG:
            case Bowmaster.HURRICANE:
            case Marksman.PIERCING_ARROW:
            case Corsair.RAPID_FIRE:
            case WindArcher.HURRICANE:
            case Evan.FIRE_BREATH:
            case Evan.ICE_BREATH:
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PacketCreator.skillCancel(c.getPlayer(), sourceid), false);
                break;

            default:
                c.getPlayer().cancelEffect(SkillFactory.getSkill(sourceid).getEffect(1), false, -1);
                break;
        }
    }
}
