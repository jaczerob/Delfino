package dev.jaczerob.delfino.network.packets.logging;

import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import dev.jaczerob.delfino.network.tools.HexTool;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Sharable
@Component
public class InPacketLogger extends ChannelInboundHandlerAdapter implements PacketLogger {
    private static final Logger log = LoggerFactory.getLogger(InPacketLogger.class);
    private static final int LOG_CONTENT_THRESHOLD = 3_000;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof InPacket packet) {
            log(packet);
        }

        ctx.fireChannelRead(msg);
    }

    @Override
    public void log(final Packet packet) {
        final var content = packet.getBytes();
        final var packetLength = content.length;

        if (packetLength > LOG_CONTENT_THRESHOLD) {
            log.debug(HexTool.toHexString(new byte[]{content[0], content[1]}) + " ...");
            return;
        }

        final var opcodeValue = LoggingUtil.readFirstShort(content);
        final var opcode = RecvOpcode.fromValue(opcodeValue);

        final var opcodeName = opcode == null ? "UNKNOWN_OPCODE" : opcode.name();
        final var logOpcode = opcode == null || opcode.getLog();

        if (!logOpcode) {
            log.trace("Sending {} packet to client: {} ... {}", opcodeName, HexTool.toHexString(content), HexTool.toStringFromAscii(content));
        } else {
            log.debug("Sending {} packet to client: {} ... {}", opcodeName, HexTool.toHexString(content), HexTool.toStringFromAscii(content));
        }
    }
}
