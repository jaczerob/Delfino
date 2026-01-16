package dev.jaczerob.delfino.elm.events.loggedin.serverlistrequest;

import dev.jaczerob.delfino.common.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedin.AbstractLoggedInEventHandler;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.stereotype.Component;

@Component
public class ServerListRequestEventHandler extends AbstractLoggedInEventHandler<Object, ServerListRequestEvent> {
    private final DelfinoConfigurationProperties delfinoConfigurationProperties;

    public ServerListRequestEventHandler(
            final SessionCoordinator sessionCoordinator,
            final DelfinoConfigurationProperties delfinoConfigurationProperties
    ) {
        super(sessionCoordinator);
        this.delfinoConfigurationProperties = delfinoConfigurationProperties;
    }

    @Override
    protected void handleEventInternal(final ServerListRequestEvent event) {
        event.getContext().writeAndFlush(this.getServerList());
        event.getContext().writeAndFlush(this.getEndOfServerList());
        event.getContext().writeAndFlush(this.selectWorld(0));
        event.getContext().writeAndFlush(this.sendRecommended());
    }

    public Packet getServerList() {
        final var packet = OutPacket.create(SendOpcode.SERVERLIST)
                .writeByte(this.delfinoConfigurationProperties.getWorld().getId())
                .writeString(this.delfinoConfigurationProperties.getWorld().getName())
                .writeByte(this.delfinoConfigurationProperties.getWorld().getFlag())
                .writeString(this.delfinoConfigurationProperties.getWorld().getEventMessage())
                .writeByte(100)
                .writeByte(0)
                .writeByte(100)
                .writeByte(0)
                .writeByte(0)
                .writeByte(this.delfinoConfigurationProperties.getWorld().getChannels().size());

        for (final var ch : this.delfinoConfigurationProperties.getWorld().getChannels())
            packet.writeString(this.delfinoConfigurationProperties.getWorld().getName() + "-" + ch.getId())
                    .writeInt(ch.getCapacity())
                    .writeByte(this.delfinoConfigurationProperties.getWorld().getId())
                    .writeByte(ch.getId() - 1)
                    .writeBool(false);

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
