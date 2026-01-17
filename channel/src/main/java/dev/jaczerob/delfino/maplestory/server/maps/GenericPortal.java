/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation version 3 as published by
the Free Software Foundation. You may not use, modify or distribute
this program under any other version of the GNU Affero General Public
License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.id.MapId;
import dev.jaczerob.delfino.maplestory.scripting.portal.PortalScriptManager;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;

import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GenericPortal implements Portal {
    private final int type;
    private String name;
    private String target;
    private Point position;
    private int targetmap;
    private boolean status = true;
    private int id;
    private String scriptName;
    private boolean portalState;
    private Lock scriptLock = null;

    public GenericPortal(int type) {
        this.type = type;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean getPortalStatus() {
        return status;
    }

    @Override
    public void setPortalStatus(boolean newStatus) {
        this.status = newStatus;
    }

    @Override
    public int getTargetMapId() {
        return targetmap;
    }

    public void setTargetMapId(int targetmapid) {
        this.targetmap = targetmapid;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public String getScriptName() {
        return scriptName;
    }

    @Override
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;

        if (scriptName != null) {
            if (scriptLock == null) {
                scriptLock = new ReentrantLock(true);
            }
        } else {
            scriptLock = null;
        }
    }

    @Override
    public void enterPortal(Client c) {
        boolean changed = false;
        if (getScriptName() != null) {
            try {
                scriptLock.lock();
                try {
                    changed = PortalScriptManager.getInstance().executePortalScript(this, c);
                } finally {
                    scriptLock.unlock();
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        } else if (getTargetMapId() != MapId.NONE) {
            Character chr = c.getPlayer();
            MapleMap to = c.getChannelServer().getMapFactory().getMap(getTargetMapId());
            Portal pto = to.getPortal(getTarget());
            if (pto == null) {
                pto = to.getPortal(0);
            }
            chr.changeMap(to, pto);
            changed = true;
        }
        if (!changed) {
            c.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }

    @Override
    public boolean getPortalState() {
        return portalState;
    }

    @Override
    public void setPortalState(boolean state) {
        this.portalState = state;
    }
}
