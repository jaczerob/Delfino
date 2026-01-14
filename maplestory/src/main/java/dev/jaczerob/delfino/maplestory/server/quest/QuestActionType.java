package dev.jaczerob.delfino.maplestory.server.quest;

/**
 * @author Matze
 */
public enum QuestActionType {
    UNDEFINED(-1),
    EXP(0),
    ITEM(1),
    NEXTQUEST(2),
    MESO(3),
    QUEST(4),
    SKILL(5),
    FAME(6),
    BUFF(7),
    PETSKILL(8),
    YES(9),
    NO(10),
    NPC(11),
    MIN_LEVEL(12),
    NORMAL_AUTO_START(13),
    PETTAMENESS(14),
    PETSPEED(15),
    INFO(16),
    ZERO(16);

    final byte type;

    QuestActionType(int type) {
        this.type = (byte) type;
    }

    public static QuestActionType getByWZName(String name) {
        switch (name) {
            case "exp":
                return EXP;
            case "money":
                return MESO;
            case "item":
                return ITEM;
            case "skill":
                return SKILL;
            case "nextQuest":
                return NEXTQUEST;
            case "pop":
                return FAME;
            case "buffItemID":
                return BUFF;
            case "petskill":
                return PETSKILL;
            case "no":
                return NO;
            case "yes":
                return YES;
            case "npc":
                return NPC;
            case "lvmin":
                return MIN_LEVEL;
            case "normalAutoStart":
                return NORMAL_AUTO_START;
            case "pettameness":
                return PETTAMENESS;
            case "petspeed":
                return PETSPEED;
            case "info":
                return INFO;
            case "0":
                return ZERO;
            default:
                return UNDEFINED;
        }
    }
}
