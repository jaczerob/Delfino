package dev.jaczerob.delfino.mrds.grpc;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.Channel;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.WorldsResponse;
import dev.jaczerob.delfino.mrds.config.DelfinoConfigurationProperties;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorldServerService extends WorldServiceGrpc.WorldServiceImplBase {
    private final DelfinoConfigurationProperties delfinoConfigurationProperties;

    public WorldServerService(final DelfinoConfigurationProperties delfinoConfigurationProperties) {
        this.delfinoConfigurationProperties = delfinoConfigurationProperties;
    }

    @Override
    public void getWorlds(final Empty request, final StreamObserver<WorldsResponse> responseObserver) {
        final var worldResponseWorld = this.fromProperties();

        final var worldsResponse = WorldsResponse.newBuilder()
                .addAllWorlds(List.of(worldResponseWorld))
                .build();

        responseObserver.onNext(worldsResponse);
        responseObserver.onCompleted();
    }

    private World fromProperties() {
        final var propertyWorld = this.delfinoConfigurationProperties.getWorld();
        final var channels = propertyWorld.getChannels().stream()
                .map(this::fromServerChannel)
                .toList();

        return World.newBuilder()
                .setId(propertyWorld.getId())
                .setName(propertyWorld.getName())
                .setFlag(propertyWorld.getFlag())
                .setEventMessage(propertyWorld.getMessages().getEvent())
                .addAllChannels(channels)
                .setAmountPlayers(1)
                .build();
    }

    private Channel fromServerChannel(final DelfinoConfigurationProperties.Channel propertyChannel) {
        return Channel.newBuilder()
                .setId(propertyChannel.getId())
                .setCapacity(propertyChannel.getCapacity())
                .build();
    }
}
