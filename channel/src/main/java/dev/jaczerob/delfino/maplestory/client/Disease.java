package dev.jaczerob.delfino.maplestory.client;

import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.server.life.MobSkillType;

import java.util.Arrays;

public enum Disease {
    NULL(0x0),
    SLOW(0x1, MobSkillType.SLOW),
    SEDUCE(0x80, MobSkillType.SEDUCE),
    FISHABLE(0x100),
    ZOMBIFY(0x4000),
    CONFUSE(0x80000, MobSkillType.REVERSE_INPUT),
    STUN(0x2000000000000L, MobSkillType.STUN),
    POISON(0x4000000000000L, MobSkillType.POISON),
    SEAL(0x8000000000000L, MobSkillType.SEAL),
    DARKNESS(0x10000000000000L, MobSkillType.DARKNESS),
    WEAKEN(0x4000000000000000L, MobSkillType.WEAKNESS),
    CURSE(0x8000000000000000L, MobSkillType.CURSE);

    private final long i;
    private final MobSkillType mobSkillType;

    Disease(long i) {
        this(i, null);
    }

    Disease(long i, MobSkillType skill) {
        this.i = i;
        this.mobSkillType = skill;
    }

    public long getValue() {
        return i;
    }

    public boolean isFirst() {
        return false;
    }

    public MobSkillType getMobSkillType() {
        return mobSkillType;
    }

    public static Disease ordinal(int ord) {
        try {
            return Disease.values()[ord];
        } catch (IndexOutOfBoundsException io) {
            return NULL;
        }
    }

    public static final Disease getRandom() {
        Disease[] diseases = GameConstants.CPQ_DISEASES;
        return diseases[(int) (Math.random() * diseases.length)];
    }

    public static final Disease getBySkill(MobSkillType skill) {
        if (skill == null) {
            return null;
        }
        return Arrays.stream(Disease.values())
                .filter(d -> d.mobSkillType == skill)
                .findAny()
                .orElse(null);
    }

}