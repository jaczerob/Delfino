package dev.jaczerob.delfino.login.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class LoginServer {
    private Channel channel;

    @PostConstruct
    public void start() {
        final var parentGroup = new MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors(), Thread.ofVirtual().factory(), NioIoHandler.newFactory());
        final var childGroup = new MultiThreadIoEventLoopGroup(Runtime.getRuntime().availableProcessors(), Thread.ofVirtual().factory(), NioIoHandler.newFactory());
        final var bootstrap = new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new LoginServerInitializer());

        this.channel = bootstrap.bind(8484).syncUninterruptibly().channel();
    }

    @PreDestroy
    public void stop() {
        if (this.channel == null) {
            throw new IllegalStateException("Must start LoginServer before stopping it");
        }

        this.channel.close().syncUninterruptibly();
    }
}
