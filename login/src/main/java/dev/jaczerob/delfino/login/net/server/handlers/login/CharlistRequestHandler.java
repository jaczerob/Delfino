package dev.jaczerob.delfino.login.net.server.handlers.login;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.net.AbstractPacketHandler;
import dev.jaczerob.delfino.login.net.opcodes.RecvOpcode;
import dev.jaczerob.delfino.login.net.packet.InPacket;
import dev.jaczerob.delfino.login.net.server.ServerStatus;
import dev.jaczerob.delfino.login.tools.PacketCreator;
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
    public void handlePacket(final InPacket p, final Client c) {
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
            c.sendPacket(PacketCreator.getServerStatus(ServerStatus.FULL.getCode()));
            return;
        }

        final var world = worlds.get(worldIndex);
        final var channelIndex = p.readByte();
        if (channelIndex >= world.getChannelsCount()) {
            log.warn("Received server status request for invalid channel index: {} for world index: {}", channelIndex, worldIndex);
            c.sendPacket(PacketCreator.getServerStatus(ServerStatus.FULL.getCode()));
            return;
        }

        c.setWorld(worldIndex);
        c.setChannel(channelIndex);
        c.sendPacket(PacketCreator.getCharList(c, 0));
    }
}