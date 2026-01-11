package dev.jaczerob.delfino.login.packets.handlers.login;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.server.LoginServer;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class CharSelectedWithPicHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(CharSelectedWithPicHandler.class);

    private final LoginServer server;

    public CharSelectedWithPicHandler(final LoginServer server) {
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHAR_SELECT_WITH_PIC;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        p.readString();
        final var charId = p.readInt();
        p.readString();
        p.readString();

        final var socket = this.server.getInetSocket();
        if (socket == null) {
            c.sendPacket(LoginPacketCreator.getInstance().getAfterLoginError(10));
            return;
        }

        try {
            c.sendPacket(LoginPacketCreator.getInstance().getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (final UnknownHostException exc) {
            log.error("Failed to resolve server address", exc);
        }
    }
}
