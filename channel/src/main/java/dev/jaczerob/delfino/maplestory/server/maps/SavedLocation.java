package dev.jaczerob.delfino.maplestory.server.maps;

public class SavedLocation {
    private final int mapId;
    private final int portal;

    public SavedLocation(int mapId, int portal) {
        this.mapId = mapId;
        this.portal = portal;
    }

    public int getMapId() {
        return mapId;
    }

    public int getPortal() {
        return portal;
    }
}
