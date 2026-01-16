package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatus;
import dev.jaczerob.delfino.maplestory.client.status.MonsterStatusEffect;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.life.Monster;
import dev.jaczerob.delfino.maplestory.server.life.MonsterInformationProvider;
import dev.jaczerob.delfino.maplestory.server.maps.MapleMap;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Jay Estrella
 * @author Ronan
 */
@Component
public final class MobDamageMobHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(MobDamageMobHandler.class);

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.MOB_DAMAGE_MOB;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int from = packet.readInt();
        packet.readInt();
        int to = packet.readInt();
        boolean magic = packet.readByte() == 0;
        int dmg = packet.readInt();
        Character chr = client.getPlayer();

        MapleMap map = chr.getMap();
        Monster attacker = map.getMonsterByOid(from);
        Monster damaged = map.getMonsterByOid(to);

        if (attacker == null || damaged == null) {
            return;
        }

        int maxDmg = calcMaxDamage(attacker, damaged, magic);     // thanks Darter (YungMoozi) for reporting unchecked dmg

        if (dmg > maxDmg) {
            AutobanFactory.DAMAGE_HACK.alert(client.getPlayer(), "Possible packet editing hypnotize damage exploit.");   // thanks Rien dev team
            String attackerName = MonsterInformationProvider.getInstance().getMobNameFromId(attacker.getId());
            String damagedName = MonsterInformationProvider.getInstance().getMobNameFromId(damaged.getId());
            log.warn("Chr {} had hypnotized {} to attack {} with damage {} (max: {})", client.getPlayer().getName(),
                    attackerName, damagedName, dmg, maxDmg);
            dmg = maxDmg;
        }

        map.damageMonster(chr, damaged, dmg);
        map.broadcastMessage(chr, ChannelPacketCreator.getInstance().damageMonster(to, dmg), false);

    }

    private static int calcMaxDamage(Monster attacker, Monster damaged, boolean magic) {
        int attackerAtk, damagedDef, attackerLevel = attacker.getLevel();
        double maxDamage;
        if (magic) {
            int atkRate = calcModifier(attacker, MonsterStatus.MAGIC_ATTACK_UP, MonsterStatus.MATK);
            attackerAtk = (attacker.getStats().getMADamage() * atkRate) / 100;

            int defRate = calcModifier(damaged, MonsterStatus.MAGIC_DEFENSE_UP, MonsterStatus.MDEF);
            damagedDef = (damaged.getStats().getMDDamage() * defRate) / 100;

            maxDamage = ((attackerAtk * (1.15 + (0.025 * attackerLevel))) - (0.75 * damagedDef)) * (Math.log(Math.abs(damagedDef - attackerAtk)) / Math.log(12));
        } else {
            int atkRate = calcModifier(attacker, MonsterStatus.WEAPON_ATTACK_UP, MonsterStatus.WATK);
            attackerAtk = (attacker.getStats().getPADamage() * atkRate) / 100;

            int defRate = calcModifier(damaged, MonsterStatus.WEAPON_DEFENSE_UP, MonsterStatus.WDEF);
            damagedDef = (damaged.getStats().getPDDamage() * defRate) / 100;

            maxDamage = ((attackerAtk * (1.15 + (0.025 * attackerLevel))) - (0.75 * damagedDef)) * (Math.log(Math.abs(damagedDef - attackerAtk)) / Math.log(17));
        }

        return (int) maxDamage;
    }

    private static int calcModifier(Monster monster, MonsterStatus buff, MonsterStatus nerf) {
        int atkModifier;
        final Map<MonsterStatus, MonsterStatusEffect> monsterStati = monster.getStati();

        MonsterStatusEffect atkBuff = monsterStati.get(buff);
        if (atkBuff != null) {
            atkModifier = atkBuff.getStati().get(buff);
        } else {
            atkModifier = 100;
        }

        MonsterStatusEffect atkNerf = monsterStati.get(nerf);
        if (atkNerf != null) {
            atkModifier -= atkNerf.getStati().get(nerf);
        }

        return atkModifier;
    }
}
