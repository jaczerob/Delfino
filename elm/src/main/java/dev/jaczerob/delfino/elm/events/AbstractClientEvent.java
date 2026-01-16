package dev.jaczerob.delfino.elm.events;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class AbstractClientEvent<T> extends ApplicationEvent {
    private final T payload;
    private final Client client;
    private final ChannelHandlerContext context;

    public AbstractClientEvent(
            final InPacket inPacket,
            final Client client,
            final ChannelHandlerContext context
    ) {
        super(client);
        this.payload = this.parsePayload(inPacket);
        this.client = client;
        this.context = context;
    }

    protected abstract T parsePayload(final InPacket inPacket);

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
