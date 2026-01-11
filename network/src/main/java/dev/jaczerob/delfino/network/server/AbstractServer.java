package dev.jaczerob.delfino.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServer {
    private static final Logger log = LoggerFactory.getLogger(AbstractServer.class);

    private Channel channel = null;

    private final int port;
    private final ServerChannelInitializer serverChannelInitializer;

    public AbstractServer(
            final int port,
            final ServerChannelInitializer serverChannelInitializer
    ) {
        this.port = port;
        this.serverChannelInitializer = serverChannelInitializer;
    }

    protected ServerChannelInitializer getServerChannelInitializer() {
        return this.serverChannelInitializer;
    }

    protected int getPort() {
        return this.port;
    }

    protected void startServer() {
        final var parentGroup = new MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors(), Thread.ofVirtual().factory(), NioIoHandler.newFactory());
        final var childGroup = new MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors(), Thread.ofVirtual().factory(), NioIoHandler.newFactory());
        final var bootstrap = new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(this.serverChannelInitializer);

        this.channel = bootstrap.bind(this.port).syncUninterruptibly().channel();
        log.info("Server started on port {}", this.port);
    }

    protected void stopServer() {
        if (this.channel != null) {
            this.channel.close().syncUninterruptibly();
            log.info("Server on port {} stopped", this.port);
        }
    }
}
