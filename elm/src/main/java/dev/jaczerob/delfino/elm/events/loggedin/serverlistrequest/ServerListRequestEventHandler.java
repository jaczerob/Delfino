package dev.jaczerob.delfino.elm.events.loggedin.serverlistrequest;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedin.AbstractLoggedInEventHandler;
import dev.jaczerob.delfino.grpc.proto.world.WorldServiceGrpc;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import io.grpc.StatusException;
import org.springframework.stereotype.Component;

@Component
public class ServerListRequestEventHandler extends AbstractLoggedInEventHandler<Object, ServerListRequestEvent> {
    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldService;

    public ServerListRequestEventHandler(
            final SessionCoordinator sessionCoordinator,
            final WorldServiceGrpc.WorldServiceBlockingV2Stub worldService
    ) {
        super(sessionCoordinator);
        this.worldService = worldService;
    }

    @Override
    protected void handleEventInternal(final ServerListRequestEvent event) {
        try {
            event.getContext().writeAndFlush(this.getServerList());
            event.getContext().writeAndFlush(this.getEndOfServerList());
            event.getContext().writeAndFlush(this.selectWorld(0));
            event.getContext().writeAndFlush(this.sendRecommended());
        } catch (final Exception exc) {
            this.getLogger().error("Failed to handle server list request event", exc);
        }
    }

    public Packet getServerList() throws StatusException {
        final var world = this.worldService.getWorld(Empty.newBuilder().build());

        final var packet = OutPacket.create(SendOpcode.SERVERLIST)
                .writeByte(world.getId())
                .writeString(world.getName())
                .writeByte(world.getFlag())
                .writeString(world.getMessages().getEvent())
                .writeByte(100)
                .writeByte(0)
                .writeByte(100)
                .writeByte(0)
                .writeByte(0)
                .writeByte(world.getChannelsCount());

        for (var i = 0; i < world.getChannelsCount(); i++) {
            final var channel = world.getChannels(i);
            packet.writeString("%s-%d".formatted(world.getName(), i + 1))
                    .writeInt(channel.getCapacity())
                    .writeByte(world.getId())
                    .writeByte(i)
                    .writeBool(false);
        }

        return packet.writeShort(0);
    }

    public Packet getEndOfServerList() {
        return OutPacket.create(SendOpcode.SERVERLIST)
                .writeByte(0xFF);
    }

    public Packet selectWorld(int world) {
        return OutPacket.create(SendOpcode.LAST_CONNECTED_WORLD)
                .writeInt(world);
    }

    public Packet sendRecommended() {
        return OutPacket.create(SendOpcode.RECOMMENDED_WORLD_MESSAGE)
                .writeByte(0);
    }
}
