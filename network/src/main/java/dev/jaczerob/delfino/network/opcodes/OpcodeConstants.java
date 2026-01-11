package dev.jaczerob.delfino.network.opcodes;

import java.util.HashMap;
import java.util.Map;

public class OpcodeConstants {
    private static final Map<Integer, String> SEND_OPCODE_NAMES = new HashMap<>();
    private static final Map<Integer, String> RECEIVE_OPCODE_NAMES = new HashMap<>();

    static {
        for (final var op : SendOpcode.values()) {
            SEND_OPCODE_NAMES.put(op.getValue(), op.name());
        }

        for (final var op : RecvOpcode.values()) {
            RECEIVE_OPCODE_NAMES.put(op.getValue(), op.name());
        }
    }

    public static String getSendOpcodeName(final int opcode) {
        return SEND_OPCODE_NAMES.get(opcode);
    }

    public static String getReceiveOpcodeName(final int opcode) {
        return RECEIVE_OPCODE_NAMES.get(opcode);
    }
}
