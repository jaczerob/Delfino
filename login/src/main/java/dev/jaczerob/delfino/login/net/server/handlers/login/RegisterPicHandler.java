package dev.jaczerob.delfino.login.net.server.handlers.login;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.AbstractPacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.net.server.Server;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public final class RegisterPicHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(RegisterPicHandler.class);

    private final Server server;

    public RegisterPicHandler(final Server server) {
        this.server = server;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.REGISTER_PIC;
    }

    @Override
    public void handlePacket(final InPacket p, final Client c) {
        p.readByte();
        final var charId = p.readInt();

        p.readString();
        p.readString();

        final var pic = p.readString();
        if (c.getPic() == null || c.getPic().isEmpty()) {
            c.setPic(pic);

            final var socket = this.server.getInetSocket();
            if (socket == null) {
                c.sendPacket(PacketCreator.getAfterLoginError(10));
                return;
            }

            try {
                c.sendPacket(PacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } catch (final UnknownHostException exc) {
                log.error("Failed to resolve login server address", exc);
            }
        } else {
            SessionCoordinator.getInstance().closeSession(c, true);
        }
    }
}