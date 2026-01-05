package dev.jaczerob.delfino.maplestory.net.packet.out;

import dev.jaczerob.delfino.maplestory.net.opcodes.SendOpcode;
import dev.jaczerob.delfino.maplestory.net.packet.ByteBufOutPacket;

public final class SendNoteSuccessPacket extends ByteBufOutPacket {

    public SendNoteSuccessPacket() {
        super(SendOpcode.MEMO_RESULT);

        writeByte(4);
    }
}
