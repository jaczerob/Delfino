package dev.jaczerob.delfino.login.net.server.handlers.login;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.AbstractPacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.net.server.Server;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator.AntiMulticlientResult;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class CharSelectedWithPicHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(CharSelectedWithPicHandler.class);

    private static int parseAntiMulticlientError(AntiMulticlientResult res) {
        return switch (res) {
            case REMOTE_PROCESSING -> 10;
            case REMOTE_LOGGEDIN -> 7;
            case REMOTE_NO_MATCH -> 17;
            case COORDINATOR_ERROR -> 8;
            default -> 9;
        };
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHAR_SELECT_WITH_PIC;
    }

    @Override
    public void handlePacket(final InPacket p, final Client c) {
        p.readString();
        final var charId = p.readInt();
        p.readString();
        p.readString();

        Server.getInstance().unregisterLoginState(c);
        c.setCharacterOnSessionTransitionState(charId);

        try {
            c.sendPacket(PacketCreator.getServerIP(InetAddress.getByName("127.0.0.1"), 7575, charId));
        } catch (UnknownHostException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
