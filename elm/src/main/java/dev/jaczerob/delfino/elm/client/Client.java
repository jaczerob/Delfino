package dev.jaczerob.delfino.elm.client;

import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.AbstractClientEvent;
import dev.jaczerob.delfino.elm.events.loggedin.characterlistrequest.CharacterListRequestEvent;
import dev.jaczerob.delfino.elm.events.loggedin.characterselected.CharacterSelectedEvent;
import dev.jaczerob.delfino.elm.events.loggedin.serverlistrequest.ServerListRequestEvent;
import dev.jaczerob.delfino.elm.events.loggedin.serverstatus.ServerStatusRequestEvent;
import dev.jaczerob.delfino.elm.events.loggedout.accepttos.AcceptToSEvent;
import dev.jaczerob.delfino.elm.events.loggedout.loginpassword.LoginPasswordEvent;
import dev.jaczerob.delfino.elm.events.loggedout.relog.RelogEvent;
import dev.jaczerob.delfino.elm.events.stateless.hello.HelloEvent;
import dev.jaczerob.delfino.elm.events.stateless.ping.PingEvent;
import dev.jaczerob.delfino.elm.events.stateless.pong.PongEvent;
import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.InvalidPacketHeaderException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;

@Getter
@Setter
public class Client extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    private Account account;
    private Channel ioChannel;

    public Client(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void channelActive(final ChannelHandlerContext context) {
        this.ioChannel = context.channel();
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        log.info("Client disconnected: {}", this.ioChannel.remoteAddress());
        SessionCoordinator.getInstance().logout(this);
    }

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) throws Exception {
        MDC.clear();
        MDC.put("client.account.id", this.account != null ? String.valueOf(this.account.getId()) : "null");

        if (!(message instanceof InPacket inPacket)) {
            log.warn("Received invalid packet: {}", message);
            return;
        }

        final var opcodeValue = inPacket.readShort();
        final var opcode = RecvOpcode.fromValue(opcodeValue);
        if (opcode == null) {
            log.warn("Unknown opcode 0x{} received", Integer.toHexString(opcodeValue));
            return;
        }

        MDC.put("packet.name", opcode.name());
        MDC.put("packet.opcode", String.format("0x%02X", opcodeValue));

        log.info("Received packet");

        final AbstractClientEvent<?> event = switch (opcode) {
            case CLIENT_HELLO -> new HelloEvent(inPacket, this, context);
            case ACCEPT_TOS -> new AcceptToSEvent(inPacket, this, context);
            case LOGIN_PASSWORD -> new LoginPasswordEvent(inPacket, this, context);
            case RELOG -> new RelogEvent(inPacket, this, context);
            case PONG -> new PongEvent(inPacket, this, context);
            case SERVER_LIST_REQUEST, SERVER_LIST_REREQUEST -> new ServerListRequestEvent(inPacket, this, context);
            case CHARACTER_LIST_REQUEST -> new CharacterListRequestEvent(inPacket, this, context);
            case SERVER_STATUS_REQUEST -> new ServerStatusRequestEvent(inPacket, this, context);
            case CHAR_SELECT_WITH_PIC -> new CharacterSelectedEvent(inPacket, this, context);
            default -> null;
        };

        if (event == null) {
            log.warn("No event found for packet");
            return;
        }

        MDC.put("event.name", event.getClass().getSimpleName());

        log.info("Publishing event");
        this.applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) {
        if (!(event instanceof IdleStateEvent)) {
            return;
        }

        MDC.clear();
        log.info("User event triggered for client: {}", event);
        this.applicationEventPublisher.publishEvent(new PingEvent(null, this, context));
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) throws Exception {
        log.warn("Exception caught by client", cause);

        if (cause instanceof InvalidPacketHeaderException || cause instanceof IOException) {
            SessionCoordinator.getInstance().logout(this);
        }
    }
}
