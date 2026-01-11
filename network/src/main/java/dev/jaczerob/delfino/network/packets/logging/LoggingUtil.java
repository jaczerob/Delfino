package dev.jaczerob.delfino.network.packets.logging;

import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import io.netty.buffer.Unpooled;

import java.util.Set;

public class LoggingUtil {
    private static final Set<Short> IGNORED_DEBUG_RECV_PACKETS = Set.of(
            (short) RecvOpcode.MOVE_PLAYER.getValue(), // 41
            (short) RecvOpcode.HEAL_OVER_TIME.getValue(), // 89
            (short) RecvOpcode.SPECIAL_MOVE.getValue(), // 91
            (short) RecvOpcode.QUEST_ACTION.getValue(), // 107
            (short) RecvOpcode.MOVE_PET.getValue(), // 167
            (short) RecvOpcode.MOVE_LIFE.getValue(), // 188
            (short) RecvOpcode.NPC_ACTION.getValue() // 197
    );

    public static short readFirstShort(byte[] bytes) {
        return Unpooled.wrappedBuffer(bytes).readShortLE();
    }

    public static boolean isIgnoredRecvPacket(short opcode) {
        return IGNORED_DEBUG_RECV_PACKETS.contains(opcode);
    }
}
