package dev.jaczerob.delfino.network.server;

import dev.jaczerob.delfino.network.encryption.ClientCyphers;
import dev.jaczerob.delfino.network.encryption.InitializationVector;
import dev.jaczerob.delfino.network.encryption.PacketCodec;
import dev.jaczerob.delfino.network.tools.PacketCreator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public abstract class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger log = LoggerFactory.getLogger(ServerChannelInitializer.class);

    private final ChannelHandler sendPacketLogger;
    private final ChannelHandler receivePacketLogger;
    private final int idleTimeSeconds;
    private final boolean logPackets;
    private final PacketCreator packetCreator;
    private final short mapleVersion;

    protected ServerChannelInitializer(
            final ChannelHandler sendPacketLogger,
            final ChannelHandler receivePacketLogger,
            final int idleTimeSeconds,
            final boolean logPackets,
            final PacketCreator packetCreator,
            final short mapleVersion
    ) {
        this.sendPacketLogger = sendPacketLogger;
        this.receivePacketLogger = receivePacketLogger;
        this.idleTimeSeconds = idleTimeSeconds;
        this.logPackets = logPackets;
        this.packetCreator = packetCreator;
        this.mapleVersion = mapleVersion;
    }

    protected String getRemoteAddress(final Channel channel) {
        String remoteAddress = "null";
        try {
            remoteAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        } catch (NullPointerException npe) {
            log.warn("Unable to get remote address from netty Channel: {}", channel, npe);
        }

        return remoteAddress;
    }

    protected void initPipeline(final SocketChannel socketChannel, final ChannelInboundHandlerAdapter client) {
        final InitializationVector sendIv = InitializationVector.generateSend();
        final InitializationVector recvIv = InitializationVector.generateReceive();
        writeInitialUnencryptedHelloPacket(socketChannel, sendIv, recvIv);
        setUpHandlers(socketChannel.pipeline(), sendIv, recvIv, client);
    }

    private void writeInitialUnencryptedHelloPacket(
            final SocketChannel socketChannel,
            final InitializationVector sendIv,
            final InitializationVector recvIv
    ) {
        socketChannel.writeAndFlush(Unpooled.wrappedBuffer(this.packetCreator.getHello(sendIv, recvIv).getBytes()));
    }

    private void setUpHandlers(
            final ChannelPipeline pipeline,
            final InitializationVector sendIv,
            final InitializationVector recvIv,
            final ChannelInboundHandlerAdapter client
    ) {
        pipeline.addLast("IdleStateHandler", new IdleStateHandler(0, 0, this.idleTimeSeconds));
        pipeline.addLast("PacketCodec", new PacketCodec(ClientCyphers.of(this.mapleVersion, sendIv, recvIv)));
        pipeline.addLast("Client", client);

        if (this.logPackets) {
            pipeline.addBefore("Client", "SendPacketLogger", this.sendPacketLogger);
            pipeline.addBefore("Client", "ReceivePacketLogger", this.receivePacketLogger);
        }
    }
}