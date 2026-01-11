package dev.jaczerob.delfino.network.encryption;

public class ClientCyphers {
    private final MapleAESOFB send;
    private final MapleAESOFB receive;

    private ClientCyphers(
            final MapleAESOFB send,
            final MapleAESOFB receive
    ) {
        this.send = send;
        this.receive = receive;
    }

    public static ClientCyphers of(
            final short mapleVersion,
            final InitializationVector sendIv,
            final InitializationVector receiveIv
    ) {
        MapleAESOFB send = new MapleAESOFB(sendIv, (short) (0xFFFF - mapleVersion));
        MapleAESOFB receive = new MapleAESOFB(receiveIv, mapleVersion);
        return new ClientCyphers(send, receive);
    }

    public MapleAESOFB getSendCypher() {
        return send;
    }

    public MapleAESOFB getReceiveCypher() {
        return receive;
    }
}
