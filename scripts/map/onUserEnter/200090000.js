// Author: Ronan
var mapId = 200090000;

function start(ms) {
    var map = ms.getClient().getChannelServer().getMapFactory().getMap(mapId);

    if (map.getDocked()) {
        const ChannelPacketCreator = Java.type('dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator');
        ms.getClient().sendPacket(ChannelPacketCreator.getInstance().musicChange("Bgm04/ArabPirate"));
        ms.getClient().sendPacket(ChannelPacketCreator.getInstance().crogBoatPacket(true));
    }

    return true;
}