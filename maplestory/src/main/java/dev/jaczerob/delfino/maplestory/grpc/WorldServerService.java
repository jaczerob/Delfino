package dev.jaczerob.delfino.maplestory.grpc;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.Channel;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.WorldsResponse;
import dev.jaczerob.delfino.maplestory.constants.game.GameConstants;
import dev.jaczerob.delfino.maplestory.net.server.Server;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class WorldServerService extends WorldServiceGrpc.WorldServiceImplBase {
    private final Server server;

    public WorldServerService(final Server server) {
        this.server = server;
    }

    @Override
    public void getWorlds(final Empty request, final StreamObserver<WorldsResponse> responseObserver) {
        final var serverWorlds = this.server.getWorlds();
        final var worlds = serverWorlds.stream()
                .map(this::fromServerWorld)
                .toList();

        final var worldsResponse = WorldsResponse.newBuilder()
                .addAllWorlds(worlds)
                .build();

        responseObserver.onNext(worldsResponse);
        responseObserver.onCompleted();
    }

    private World fromServerWorld(final dev.jaczerob.delfino.maplestory.net.server.world.World serverWorld) {
        final var serverChannels = serverWorld.getChannels().stream()
                .map(this::fromServerChannel)
                .toList();

        return World.newBuilder()
                .setId(serverWorld.getId())
                .setName(GameConstants.WORLD_NAMES[serverWorld.getId()])
                .setFlag(serverWorld.getFlag())
                .setEventMessage(serverWorld.getEventMessage())
                .addAllChannels(serverChannels)
                .setAmountPlayers(serverWorld.getPlayerStorage().getSize())
                .build();
    }

    private Channel fromServerChannel(final dev.jaczerob.delfino.maplestory.net.server.channel.Channel serverChannel) {
        return Channel.newBuilder()
                .setId(serverChannel.getId())
                .setCapacity(serverChannel.getChannelCapacity())
                .build();
    }
}
