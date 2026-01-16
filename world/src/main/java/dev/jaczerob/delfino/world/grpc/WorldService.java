package dev.jaczerob.delfino.world.grpc;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.common.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.grpc.proto.world.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@RequiredArgsConstructor
@Component
public class WorldService extends WorldServiceGrpc.WorldServiceImplBase {
    private final DelfinoConfigurationProperties delfinoConfigurationProperties;

    @Override
    public void getWorld(Empty request, StreamObserver<World> responseObserver) {
        final var messages = Messages.newBuilder()
                .setEvent(this.delfinoConfigurationProperties.getWorld().getMessages().getEvent())
                .setRecommended(this.delfinoConfigurationProperties.getWorld().getMessages().getRecommended())
                .setServer(this.delfinoConfigurationProperties.getWorld().getMessages().getServer())
                .build();

        final var rates = Rates.newBuilder()
                .setExp(this.delfinoConfigurationProperties.getWorld().getRates().getExp())
                .setMeso(this.delfinoConfigurationProperties.getWorld().getRates().getMeso())
                .setDrop(this.delfinoConfigurationProperties.getWorld().getRates().getDrop())
                .setBossDrop(this.delfinoConfigurationProperties.getWorld().getRates().getBossDrop())
                .setQuest(this.delfinoConfigurationProperties.getWorld().getRates().getQuest())
                .setFishing(this.delfinoConfigurationProperties.getWorld().getRates().getFishing())
                .setTravel(this.delfinoConfigurationProperties.getWorld().getRates().getTravel())
                .build();

        final var channels = new ArrayList<Channel>();
        for (final var channel : this.delfinoConfigurationProperties.getWorld().getChannels()) {
            if (!channel.isEnabled()) {
                continue;
            }

            channels.add(
                    Channel.newBuilder()
                            .setAmountPlayers(100) // TODO: Replace with actual amount of players)
                            .setCapacity(channel.getCapacity())
                            .setPort(channel.getPort())
                            .setIp(channel.getIp())
                            .setStatus(WorldStatus.BUSY)
                            .build()
            );
        }

        final var world = World.newBuilder()
                .setId(this.delfinoConfigurationProperties.getWorld().getId())
                .setFlag(this.delfinoConfigurationProperties.getWorld().getFlag())
                .setName(this.delfinoConfigurationProperties.getWorld().getName())
                .setMessages(messages)
                .setRates(rates)
                .addAllChannels(channels)
                .build();

        responseObserver.onNext(world);
        responseObserver.onCompleted();
    }
}
