package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.provider.Data;
import dev.jaczerob.delfino.maplestory.provider.DataTool;

import java.awt.*;

public class PortalFactory {
    private int nextDoorPortal;

    public PortalFactory() {
        nextDoorPortal = 0x80;
    }

    public Portal makePortal(int type, Data portal) {
        GenericPortal ret = null;
        if (type == Portal.MAP_PORTAL) {
            ret = new MapPortal();
        } else {
            ret = new GenericPortal(type);
        }
        loadPortal(ret, portal);
        return ret;
    }

    private void loadPortal(GenericPortal myPortal, Data portal) {
        myPortal.setName(DataTool.getString(portal.getChildByPath("pn")));
        myPortal.setTarget(DataTool.getString(portal.getChildByPath("tn")));
        myPortal.setTargetMapId(DataTool.getInt(portal.getChildByPath("tm")));
        int x = DataTool.getInt(portal.getChildByPath("x"));
        int y = DataTool.getInt(portal.getChildByPath("y"));
        myPortal.setPosition(new Point(x, y));
        String script = DataTool.getString("script", portal, null);
        if (script != null && script.equals("")) {
            script = null;
        }
        myPortal.setScriptName(script);
        if (myPortal.getType() == Portal.DOOR_PORTAL) {
            myPortal.setId(nextDoorPortal);
            nextDoorPortal++;
        } else {
            myPortal.setId(Integer.parseInt(portal.getName()));
        }
    }
}
