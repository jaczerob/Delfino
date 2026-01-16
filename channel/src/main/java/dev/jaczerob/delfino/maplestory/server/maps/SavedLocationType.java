package dev.jaczerob.delfino.maplestory.server.maps;

public enum SavedLocationType {
    FREE_MARKET,
    WORLDTOUR,
    FLORINA,
    INTRO,
    SUNDAY_MARKET,
    MIRROR,
    EVENT,
    BOSSPQ,
    HAPPYVILLE,
    MONSTER_CARNIVAL,
    DEVELOPER;

    public static SavedLocationType fromString(String Str) {
        return valueOf(Str);
    }
}