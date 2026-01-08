package dev.jaczerob.delfino.login.net.netty;

import dev.jaczerob.delfino.login.client.Client;
import dev.jaczerob.delfino.login.constants.net.ServerConstants;
import dev.jaczerob.delfino.login.net.encryption.ClientCyphers;
import dev.jaczerob.delfino.login.net.encryption.InitializationVector;
import dev.jaczerob.delfino.login.net.encryption.PacketCodec;
import dev.jaczerob.delfino.login.net.packet.logging.InPacketLogger;
import dev.jaczerob.delfino.login.net.packet.logging.OutPacketLogger;
import dev.jaczerob.delfino.login.tools.PacketCreator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public abstract class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger log = LoggerFactory.getLogger(ServerChannelInitializer.class);
    private static final int IDLE_TIME_SECONDS = 30;
    private static final boolean LOG_PACKETS = true;
    private static final ChannelHandler sendPacketLogger = new OutPacketLogger();
    private static final ChannelHandler receivePacketLogger = new InPacketLogger();

    static final AtomicLong sessionId = new AtomicLong(7777);

    String getRemoteAddress(Channel channel) {
        String remoteAddress = "null";
        try {
            remoteAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        } catch (NullPointerException npe) {
            log.warn("Unable to get remote address from netty Channel: {}", channel, npe);
        }

        return remoteAddress;
    }

    void initPipeline(SocketChannel socketChannel, Client client) {
        final InitializationVector sendIv = InitializationVector.generateSend();
        final InitializationVector recvIv = InitializationVector.generateReceive();
        writeInitialUnencryptedHelloPacket(socketChannel, sendIv, recvIv);
        setUpHandlers(socketChannel.pipeline(), sendIv, recvIv, client);
    }

    private void writeInitialUnencryptedHelloPacket(SocketChannel socketChannel, InitializationVector sendIv, InitializationVector recvIv) {
        socketChannel.writeAndFlush(Unpooled.wrappedBuffer(PacketCreator.getHello(ServerConstants.VERSION, sendIv, recvIv).getBytes()));
    }

    private void setUpHandlers(ChannelPipeline pipeline, InitializationVector sendIv, InitializationVector recvIv,
                               Client client) {
        pipeline.addLast("IdleStateHandler", new IdleStateHandler(0, 0, IDLE_TIME_SECONDS));
        pipeline.addLast("PacketCodec", new PacketCodec(ClientCyphers.of(sendIv, recvIv)));
        pipeline.addLast("Client", client);

        if (LOG_PACKETS) {
            pipeline.addBefore("Client", "SendPacketLogger", sendPacketLogger);
            pipeline.addBefore("Client", "ReceivePacketLogger", receivePacketLogger);
        }
    }
}
