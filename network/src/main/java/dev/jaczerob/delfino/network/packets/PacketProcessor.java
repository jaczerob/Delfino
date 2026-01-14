package dev.jaczerob.delfino.network.packets;

import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketProcessor<T extends ChannelInboundHandlerAdapter> {
    private final Map<Integer, PacketHandler<T>> packetHandlerMap = new ConcurrentHashMap<>();

    public PacketProcessor(final List<PacketHandler<T>> packetHandlers) {
        packetHandlers.forEach(this::registerHandler);
    }

    public PacketHandler<T> getHandler(final short packetId) {
        return this.packetHandlerMap.get((int) packetId);
    }

    private void registerHandler(final PacketHandler<T> handler) {
        this.packetHandlerMap.put(handler.getOpcode().getValue(), handler);
    }
}
