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
import dev.jaczerob.delfino.maplestory.constants.skills.Bishop;
import dev.jaczerob.delfino.maplestory.constants.skills.Bowmaster;
import dev.jaczerob.delfino.maplestory.constants.skills.Brawler;
import dev.jaczerob.delfino.maplestory.constants.skills.ChiefBandit;
import dev.jaczerob.delfino.maplestory.constants.skills.Corsair;
import dev.jaczerob.delfino.maplestory.constants.skills.DarkKnight;
import dev.jaczerob.delfino.maplestory.constants.skills.Evan;
import dev.jaczerob.delfino.maplestory.constants.skills.FPArchMage;
import dev.jaczerob.delfino.maplestory.constants.skills.FPMage;
import dev.jaczerob.delfino.maplestory.constants.skills.Gunslinger;
import dev.jaczerob.delfino.maplestory.constants.skills.Hero;
import dev.jaczerob.delfino.maplestory.constants.skills.ILArchMage;
import dev.jaczerob.delfino.maplestory.constants.skills.Marksman;
import dev.jaczerob.delfino.maplestory.constants.skills.NightWalker;
import dev.jaczerob.delfino.maplestory.constants.skills.Paladin;
import dev.jaczerob.delfino.maplestory.constants.skills.ThunderBreaker;
import dev.jaczerob.delfino.maplestory.constants.skills.WindArcher;
import dev.jaczerob.delfino.maplestory.net.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.net.packet.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jaczerob.delfino.maplestory.tools.PacketCreator;

public class SkillEffectHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(SkillEffectHandler.class);

    @Override
    public void handlePacket(InPacket p, Client c) {
        int skillId = p.readInt();
        int level = p.readByte();
        byte flags = p.readByte();
        int speed = p.readByte();
        byte aids = p.readByte();//Mmmk
        switch (skillId) {
            case FPMage.EXPLOSION:
            case FPArchMage.BIG_BANG:
            case ILArchMage.BIG_BANG:
            case Bishop.BIG_BANG:
            case Bowmaster.HURRICANE:
            case Marksman.PIERCING_ARROW:
            case ChiefBandit.CHAKRA:
            case Brawler.CORKSCREW_BLOW:
            case Gunslinger.GRENADE:
            case Corsair.RAPID_FIRE:
            case WindArcher.HURRICANE:
            case NightWalker.POISON_BOMB:
            case ThunderBreaker.CORKSCREW_BLOW:
            case Paladin.MONSTER_MAGNET:
            case DarkKnight.MONSTER_MAGNET:
            case Hero.MONSTER_MAGNET:
            case Evan.FIRE_BREATH:
            case Evan.ICE_BREATH:
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PacketCreator.skillEffect(c.getPlayer(), skillId, level, flags, speed, aids), false);
                return;
            default:
                log.warn("Chr {} entered SkillEffectHandler without being handled using {}", c.getPlayer(), skillId);
                return;
        }
    }
}
