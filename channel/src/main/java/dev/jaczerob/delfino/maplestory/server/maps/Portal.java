package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.client.Client;

import java.awt.*;

public interface Portal {
    int TELEPORT_PORTAL = 1;
    int MAP_PORTAL = 2;
    int DOOR_PORTAL = 6;
    boolean OPEN = true;
    boolean CLOSED = false;

    int getType();

    int getId();

    Point getPosition();

    String getName();

    String getTarget();

    String getScriptName();

    void setScriptName(String newName);

    void setPortalStatus(boolean newStatus);

    boolean getPortalStatus();

    int getTargetMapId();

    void enterPortal(Client c);

    void setPortalState(boolean state);

    boolean getPortalState();
}
