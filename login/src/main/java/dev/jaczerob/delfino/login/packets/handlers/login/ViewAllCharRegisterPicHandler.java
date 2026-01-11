package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.server.LoginServer;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public final class ViewAllCharRegisterPicHandler extends AbstractPacketHandler {
    private final LoginServer server;

    public ViewAllCharRegisterPicHandler(final LoginServer server) {
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.VIEW_ALL_PIC_REGISTER;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client) {
        packet.readByte();
        int charId = packet.readInt();
        packet.readInt();

        packet.readString();
        packet.readString();
        packet.readString();

        final var socket = this.server.getInetSocket();
        if (socket == null) {
            client.sendPacket(LoginPacketCreator.getInstance().getAfterLoginError(10));
            return;
        }

        try {
            client.sendPacket(LoginPacketCreator.getInstance().getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (final UnknownHostException exc) {
            log.error("Failed to resolve server address", exc);
        }
    }
}
