package dev.jaczerob.delfino.login.packets.handlers.login;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.config.DelfinoConfigurationProperties;
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
public final class ServerStatusRequestHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ServerStatusRequestHandler.class);

    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub;
    private final DelfinoConfigurationProperties config;

    public ServerStatusRequestHandler(
            final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub,
            final DelfinoConfigurationProperties config
    ) {
        this.worldServiceStub = worldServiceStub;
        this.config = config;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SERVERSTATUS_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        List<World> worlds;
        try {
            worlds = this.worldServiceStub.getWorlds(Empty.newBuilder().build()).getWorldsList();
        } catch (final StatusException exc) {
            worlds = List.of();
        }

        final var worldIndex = (byte) p.readShort();
        if (worldIndex >= worlds.size()) {
            log.warn("Received server status request for invalid world index: {}", worldIndex);
            c.sendPacket(LoginPacketCreator.getInstance().getServerStatus(ServerStatus.FULL.getCode()));
            return;
        }

        final var world = worlds.get(worldIndex);
        final var status = this.getWorldCapacityStatus(world);

        c.sendPacket(LoginPacketCreator.getInstance().getServerStatus(status.getCode()));
    }

    private ServerStatus getWorldCapacityStatus(final World world) {
        if (world == null) {
            return ServerStatus.FULL;
        }

        final var worldCap = world.getChannelsCount() * this.config.getServer().getChannelLoad();
        final var num = world.getAmountPlayers();
        if (num >= worldCap) {
            return ServerStatus.FULL;
        } else if (num >= worldCap * .8) {
            return ServerStatus.BUSY;
        } else {
            return ServerStatus.NORMAL;
        }
    }
}
