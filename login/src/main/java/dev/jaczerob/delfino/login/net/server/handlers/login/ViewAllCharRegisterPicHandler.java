package dev.jaczerob.delfino.login.net.server.handlers.login;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.AbstractPacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.net.server.Server;
import dev.jaczerob.delfino.login.net.server.coordinator.session.Hwid;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator;
import dev.jaczerob.delfino.login.net.server.coordinator.session.SessionCoordinator.AntiMulticlientResult;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import dev.jaczerob.delfino.login.tools.Randomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public final class ViewAllCharRegisterPicHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ViewAllCharRegisterPicHandler.class);

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
        return RecvOpcode.VIEW_ALL_PIC_REGISTER;
    }

    @Override
    public final void handlePacket(final InPacket p, final Client c) {
        p.readByte();
        int charId = p.readInt();
        p.readInt(); // please don't let the client choose which world they should login

        String mac = p.readString();
        String hostString = p.readString();

        final Hwid hwid;
        try {
            hwid = Hwid.fromHostString(hostString);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid host string: {}", hostString, e);
            c.sendPacket(PacketCreator.getAfterLoginError(17));
            return;
        }

        c.updateMacs(mac);
        c.updateHwid(hwid);

        if (c.hasBannedMac() || c.hasBannedHWID()) {
            SessionCoordinator.getInstance().closeSession(c, true);
            return;
        }

        AntiMulticlientResult res = SessionCoordinator.getInstance().attemptGameSession(c, c.getAccID(), hwid);
        if (res != AntiMulticlientResult.SUCCESS) {
            c.sendPacket(PacketCreator.getAfterLoginError(parseAntiMulticlientError(res)));
            return;
        }

        Server server = Server.getInstance();
        if (!server.haveCharacterEntry(c.getAccID(), charId)) {
            SessionCoordinator.getInstance().closeSession(c, true);
            return;
        }

        c.setWorld(server.getCharacterWorld(charId));
        final var wserv = c.getWorldServer();
        // TODO: enable world capacity check
        if (wserv == null) {// || wserv.isWorldCapacityFull()) {
            c.sendPacket(PacketCreator.getAfterLoginError(10));
            return;
        }

        int channel = Randomizer.rand(1, server.getWorld(c.getWorld()).getChannelsCount());
        c.setChannel(channel);

        String pic = p.readString();
        c.setPic(pic);

        String[] socket = server.getInetSocket(c, c.getWorld(), channel);
        if (socket == null) {
            c.sendPacket(PacketCreator.getAfterLoginError(10));
            return;
        }

        server.unregisterLoginState(c);
        c.setCharacterOnSessionTransitionState(charId);

        try {
            c.sendPacket(PacketCreator.getServerIP(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
