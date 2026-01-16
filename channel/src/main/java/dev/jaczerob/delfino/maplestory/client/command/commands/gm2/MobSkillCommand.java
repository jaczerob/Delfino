package dev.jaczerob.delfino.maplestory.client.command.commands.gm2;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.command.Command;
import dev.jaczerob.delfino.maplestory.server.life.MobSkill;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillFactory;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillType;

import java.util.Collections;
import java.util.Optional;

public class MobSkillCommand extends Command {
    {
        setDescription("Apply a mob skill to all mobs on the map. Args: <mob skill id> <skill level>");
    }

    @Override
    public void execute(Client client, String[] params) {
        if (params.length < 2) {
            throw new IllegalArgumentException("Mob skill command requires two args: mob skill id and level");
        }

        String skillId = params[0];
        String skillLevel = params[1];
        Optional<MobSkillType> possibleType = MobSkillType.from(Integer.parseInt(skillId));
        Optional<MobSkill> possibleSkill = possibleType.map(
                type -> MobSkillFactory.getMobSkillOrThrow(type, Integer.parseInt(skillLevel))
        );
        if (possibleSkill.isEmpty()) {
            return;
        }

        Character chr = client.getPlayer();
        MobSkill mobSkill = possibleSkill.get();
        chr.getMap().getAllMonsters().forEach(
                monster -> mobSkill.applyEffect(chr, monster, false, Collections.emptyList())
        );
    }
}
