package dev.jaczerob.delfino.maplestory.client.inventory;

/**
 * @author Matze
 */
public enum InventoryType {
    UNDEFINED(0),
    EQUIP(1),
    USE(2),
    SETUP(3),
    ETC(4),
    CASH(5),
    CANHOLD(6),   //Proof-guard for inserting after removal checks
    EQUIPPED(-1); //Seems nexon screwed something when removing an item T_T

    final byte type;

    InventoryType(int type) {
        this.type = (byte) type;
    }

    public byte getType() {
        return type;
    }

    public short getBitfieldEncoding() {
        return (short) (2 << type);
    }

    public static InventoryType getByType(byte type) {
        for (InventoryType l : InventoryType.values()) {
            if (l.getType() == type) {
                return l;
            }
        }
        return null;
    }

    public static InventoryType getByWZName(String name) {
        return switch (name) {
            case "Install" -> SETUP;
            case "Consume" -> USE;
            case "Etc" -> ETC;
            case "Cash" -> CASH;
            case "Pet" -> CASH;
            default -> UNDEFINED;
        };
    }
}
