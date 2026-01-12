package dev.jaczerob.delfino.login.packets.handlers.login;

import com.google.protobuf.Empty;
import dev.jaczerob.delfino.grpc.proto.World;
import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.grpc.StatusException;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class ServerlistRequestHandler extends AbstractPacketHandler {
    private final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub;

    public ServerlistRequestHandler(
            final WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub,
            final SessionCoordinator sessionCoordinator,
            final LoginPacketCreator loginPacketCreator
    ) {
        super(sessionCoordinator, loginPacketCreator);
        this.worldServiceStub = worldServiceStub;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SERVERLIST_REQUEST;
    }

    @Override
    public void handlePacket(final InPacket packet, final LoginClient client, final ChannelHandlerContext context) {
        List<World> worlds;
        try {
            worlds = this.worldServiceStub.getWorlds(Empty.newBuilder().build()).getWorldsList();
        } catch (final StatusException exc) {
            worlds = List.of();
        }

        for (final var world : worlds) {
            context.writeAndFlush(this.loginPacketCreator.getServerList(world.getId(), world.getName(), world.getFlag(), world.getMessages().getEvent(), world.getChannelsList()));
        }

        context.writeAndFlush(this.loginPacketCreator.getEndOfServerList());
        context.writeAndFlush(this.loginPacketCreator.selectWorld(0));
        context.writeAndFlush(this.loginPacketCreator.sendRecommended(List.of()));
    }
}