package dev.jaczerob.delfino.login.packet.out;

import dev.jaczerob.delfino.login.opcodes.SendOpcode;
import dev.jaczerob.delfino.login.packet.ByteBufOutPacket;

public final class SendNoteSuccessPacket extends ByteBufOutPacket {

    public SendNoteSuccessPacket() {
        super(SendOpcode.MEMO_RESULT);

        writeByte(4);
    }
}
