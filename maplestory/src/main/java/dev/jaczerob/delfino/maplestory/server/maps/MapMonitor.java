package dev.jaczerob.delfino.maplestory.server.maps;

import dev.jaczerob.delfino.maplestory.server.TimerManager;

import java.util.concurrent.ScheduledFuture;

public class MapMonitor {
    private ScheduledFuture<?> monitorSchedule;
    private MapleMap map;
    private Portal portal;

    public MapMonitor(final MapleMap map, String portal) {
        this.map = map;
        this.portal = map.getPortal(portal);
        this.monitorSchedule = TimerManager.getInstance().register(() -> {
            if (map.getCharacters().size() < 1) {
                cancelAction();
            }
        }, 5000);
    }

    private void cancelAction() {
        if (monitorSchedule != null) {  // thanks Thora for pointing a NPE occurring here
            monitorSchedule.cancel(false);
            monitorSchedule = null;
        }

        map.killAllMonsters();
        map.clearDrops();
        if (portal != null) {
            portal.setPortalStatus(Portal.OPEN);
        }
        map.resetReactors();

        map = null;
        portal = null;
    }
}
