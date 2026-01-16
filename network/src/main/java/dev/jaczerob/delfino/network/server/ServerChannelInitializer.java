package dev.jaczerob.delfino.network.server;

import dev.jaczerob.delfino.common.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.network.encryption.ClientCyphers;
import dev.jaczerob.delfino.network.encryption.InitializationVector;
import dev.jaczerob.delfino.network.encryption.PacketCodec;
import dev.jaczerob.delfino.network.packets.ByteBufOutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public abstract class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final ChannelHandler sendPacketLogger;
    private final ChannelHandler receivePacketLogger;
    private final int idleTimeSeconds;
    private final boolean logPackets;
    private final short mapleVersion;

    protected ServerChannelInitializer(
            final ChannelHandler sendPacketLogger,
            final ChannelHandler receivePacketLogger,
            final DelfinoConfigurationProperties delfinoConfigurationProperties
    ) {
        this.sendPacketLogger = sendPacketLogger;
        this.receivePacketLogger = receivePacketLogger;
        this.idleTimeSeconds = delfinoConfigurationProperties.getNetty().getIdleTimeSeconds();
        this.logPackets = delfinoConfigurationProperties.getNetty().isLogPackets();
        this.mapleVersion = delfinoConfigurationProperties.getServer().getVersion();
    }

    @Override
    protected void initChannel(final SocketChannel socketChannel) {
        final var client = this.initClient();
        this.initPipeline(socketChannel, client);
    }

    protected abstract ChannelInboundHandlerAdapter initClient();

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
        socketChannel.writeAndFlush(Unpooled.wrappedBuffer(this.getHello(sendIv, recvIv).getBytes()));
    }

    private Packet getHello(final InitializationVector sendIv, final InitializationVector recvIv) {
        return new ByteBufOutPacket()
                .writeShort(0x0E)
                .writeShort(this.mapleVersion)
                .writeShort(1)
                .writeByte(49)
                .writeBytes(recvIv.getBytes())
                .writeBytes(sendIv.getBytes())
                .writeByte(8);

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