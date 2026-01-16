package dev.jaczerob.delfino.maplestory.net.server.task;

import dev.jaczerob.delfino.maplestory.net.server.PlayerStorage;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import dev.jaczerob.delfino.maplestory.net.server.channel.Channel;
import dev.jaczerob.delfino.maplestory.server.maps.MapManager;

/**
 * @author Resinate
 */
public class RespawnTask implements Runnable {

    @Override
    public void run() {
        for (Channel ch : Server.getInstance().getAllChannels()) {
            PlayerStorage ps = ch.getPlayerStorage();
            if (ps != null) {
                if (!ps.getAllCharacters().isEmpty()) {
                    MapManager mapManager = ch.getMapFactory();
                    if (mapManager != null) {
                        mapManager.updateMaps();
                    }
                }
            }
        }
    }
}
