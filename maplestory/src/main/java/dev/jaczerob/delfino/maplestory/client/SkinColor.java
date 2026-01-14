package dev.jaczerob.delfino.maplestory.client;

public enum SkinColor {
    LIGHT(0),
    TANNED(1),
    DARK(2),
    PALE(3),
    BLUE(4),
    GREEN(5),
    WHITE(9),
    PINK(10),
    BROWN(11);

    final int id;

    SkinColor(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static SkinColor getById(int id) {
        for (SkinColor l : SkinColor.values()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }
}
