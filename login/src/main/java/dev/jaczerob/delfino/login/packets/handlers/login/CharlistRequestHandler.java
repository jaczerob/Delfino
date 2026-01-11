package dev.jaczerob.delfino.login.packets.handlers.login;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.server.ServerStatus;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.grpc.StatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class CharlistRequestHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(CharlistRequestHandler.class);

    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub;

    public CharlistRequestHandler(final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub) {
        this.worldServiceStub = worldServiceStub;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CHARLIST_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        List<World> worlds;
        try {
            worlds = this.worldServiceStub.getWorlds(Empty.newBuilder().build()).getWorldsList();
        } catch (final StatusException exc) {
            worlds = List.of();
        }

        p.readByte();

        final var worldIndex = p.readByte();
        if (worldIndex >= worlds.size()) {
            log.warn("Received server status request for invalid world index: {}", worldIndex);
            c.sendPacket(LoginPacketCreator.getInstance().getServerStatus(ServerStatus.FULL.getCode()));
            return;
        }

        final var world = worlds.get(worldIndex);
        final var channelIndex = p.readByte();
        if (channelIndex >= world.getChannelsCount()) {
            log.warn("Received server status request for invalid channel index: {} for world index: {}", channelIndex, worldIndex);
            c.sendPacket(LoginPacketCreator.getInstance().getServerStatus(ServerStatus.FULL.getCode()));
            return;
        }

        c.sendPacket(LoginPacketCreator.getInstance().getCharList(c, 0));
    }
}