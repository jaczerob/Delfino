package dev.jaczerob.delfino.login.netty;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.encryption.ClientCyphers;
import dev.jaczerob.delfino.login.encryption.InitializationVector;
import dev.jaczerob.delfino.login.encryption.PacketCodec;
import dev.jaczerob.delfino.login.packet.ByteBufOutPacket;
import dev.jaczerob.delfino.login.packet.logging.InPacketLogger;
import dev.jaczerob.delfino.login.packet.logging.OutPacketLogger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final int IDLE_TIME_SECONDS = 30;
    private static final boolean LOG_PACKETS = true;
    private static final ChannelHandler sendPacketLogger = new OutPacketLogger();
    private static final ChannelHandler receivePacketLogger = new InPacketLogger();

    static final AtomicLong sessionId = new AtomicLong(7777);

    void initPipeline(final SocketChannel socketChannel, final LoginClient client) {
        final var sendIv = InitializationVector.generateSend();
        final var recvIv = InitializationVector.generateReceive();
        this.writeInitialUnencryptedHelloPacket(socketChannel, sendIv, recvIv);
        this.setUpHandlers(socketChannel.pipeline(), sendIv, recvIv, client);
    }

    private void writeInitialUnencryptedHelloPacket(SocketChannel socketChannel, InitializationVector sendIv, InitializationVector recvIv) {
        final var p = new ByteBufOutPacket();
        p.writeShort(0x0E);
        p.writeShort(83);
        p.writeShort(1);
        p.writeByte(49);
        p.writeBytes(recvIv.getBytes());
        p.writeBytes(sendIv.getBytes());
        p.writeByte(8);

        socketChannel.writeAndFlush(Unpooled.wrappedBuffer(p.getBytes()));
    }

    private void setUpHandlers(
            final ChannelPipeline pipeline,
            final InitializationVector sendIv,
            final InitializationVector recvIv,
            final LoginClient client
    ) {
        pipeline.addLast("IdleStateHandler", new IdleStateHandler(0, 0, IDLE_TIME_SECONDS));
        pipeline.addLast("PacketCodec", new PacketCodec(ClientCyphers.of(sendIv, recvIv)));
        pipeline.addLast("Client", client);

        if (LOG_PACKETS) {
            pipeline.addBefore("Client", "SendPacketLogger", sendPacketLogger);
            pipeline.addBefore("Client", "ReceivePacketLogger", receivePacketLogger);
        }
    }
}
