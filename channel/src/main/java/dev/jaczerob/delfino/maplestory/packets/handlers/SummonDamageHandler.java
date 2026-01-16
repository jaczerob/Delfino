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
import dev.jaczerob.delfino.maplestory.client.Skill;
import dev.jaczerob.delfino.maplestory.client.SkillFactory;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.inventory.InventoryType;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.WeaponType;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatusEffect;
import dev.jaczerob.delfino.maplestory.constants.skills.Outlaw;
import dev.jaczerob.delfino.maplestory.server.ItemInformationProvider;
import dev.jaczerob.delfino.maplestory.server.StatEffect;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.life.MonsterInformationProvider;
import dev.jaczerob.delfino.maplestory.server.maps.Summon;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Component
public final class SummonDamageHandler extends AbstractDealDamageHandler {
    private static final Logger log = LoggerFactory.getLogger(SummonDamageHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SUMMON_ATTACK;
    }

    public record SummonAttackTarget(int monsterOid, int damage, short delay) {
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int oid = packet.readInt();
        Character player = client.getPlayer();
        if (!player.isAlive()) {
            return;
        }
        Summon summon = null;
        for (Summon sum : player.getSummonsValues()) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }
        if (summon == null) {
            return;
        }
        Skill summonSkill = SkillFactory.getSkill(summon.getSkill());
        StatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        packet.skip(4);
        List<SummonAttackTarget> targets = new ArrayList<>();
        byte direction = packet.readByte();
        int numAttacked = packet.readByte();
        packet.skip(8); // I failed lol (mob x,y and summon x,y), Thanks Gerald
        for (int x = 0; x < numAttacked; x++) {
            int monsterOid = packet.readInt(); // attacked oid
            packet.skip(8);
            Point curPos = packet.readPos();
            Point nextPos = packet.readPos();
            short delay = packet.readShort();
            int damage = packet.readInt();
            targets.add(new SummonAttackTarget(monsterOid, damage, delay));
        }
        player.getMap().broadcastMessage(player, ChannelPacketCreator.getInstance().summonAttack(player.getId(), summon.getObjectId(),
                direction, targets), summon.getPosition());

        if (player.getMap().isOwnershipRestricted(player)) {
            return;
        }

        boolean magic = summonEffect.getWatk() == 0;
        int maxDmg = calcMaxDamage(summonEffect, player, magic);    // thanks Darter (YungMoozi) for reporting unchecked max dmg
        for (SummonAttackTarget target : targets) {
            int damage = target.damage();
            Monster mob = player.getMap().getMonsterByOid(target.monsterOid());
            if (mob == null) {
                continue;
            }

            if (damage > maxDmg) {
                AutobanFactory.DAMAGE_HACK.alert(client.getPlayer(), "Possible packet editing summon damage exploit.");
                final String mobName = MonsterInformationProvider.getInstance().getMobNameFromId(mob.getId());
                log.info("Possible exploit - chr {} used a summon of skillId {} to attack {} with damage {} (max: {})",
                        client.getPlayer().getName(), summon.getSkill(), mobName, damage, maxDmg);
                damage = maxDmg;
            }

            if (damage > 0 && summonEffect.getMonsterStati().size() > 0) {
                if (summonEffect.makeChanceResult()) {
                    mob.applyStatus(player, new MonsterStatusEffect(summonEffect.getMonsterStati(), summonSkill, null, false), summonEffect.isPoison(), 4000);
                }
            }
            player.getMap().damageMonster(player, mob, damage, target.delay());

        }

        if (summon.getSkill() == Outlaw.GAVIOTA) {  // thanks Periwinks for noticing Gaviota not cancelling after grenade toss
            player.cancelEffect(summonEffect, false, -1);
        }
    }

    private static int calcMaxDamage(StatEffect summonEffect, Character player, boolean magic) {
        double maxDamage;

        if (magic) {
            int matk = Math.max(player.getTotalMagic(), 14);
            maxDamage = player.calculateMaxBaseMagicDamage(matk) * (0.05 * summonEffect.getMatk());
        } else {
            int watk = Math.max(player.getTotalWatk(), 14);
            Item weapon_item = player.getInventory(InventoryType.EQUIPPED).getItem((short) -11);

            int maxBaseDmg;  // thanks Conrad, Atoot for detecting some summons legitimately hitting over the calculated limit
            if (weapon_item != null) {
                maxBaseDmg = player.calculateMaxBaseDamage(watk, ItemInformationProvider.getInstance().getWeaponType(weapon_item.getItemId()));
            } else {
                maxBaseDmg = player.calculateMaxBaseDamage(watk, WeaponType.SWORD1H);
            }

            float summonDmgMod = (maxBaseDmg >= 438) ? 0.054f : 0.077f;
            maxDamage = maxBaseDmg * (summonDmgMod * summonEffect.getWatk());
        }

        return (int) maxDamage;
    }
}
