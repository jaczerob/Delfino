package dev.jaczerob.delfino.maplestory.server.life;

import dev.jaczerob.delfino.maplestory.client.Character;

public interface MonsterListener {

    void monsterKilled(int aniTime);
    void monsterDamaged(Character from, int trueDmg);
    void monsterHealed(int trueHeal);
}
