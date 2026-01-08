package dev.jaczerob.delfino.login.net.packet.out;

import dev.jaczerob.delfino.login.net.opcodes.SendOpcode;
import dev.jaczerob.delfino.login.net.packet.ByteBufOutPacket;

public final class SendNoteSuccessPacket extends ByteBufOutPacket {

    public SendNoteSuccessPacket() {
        super(SendOpcode.MEMO_RESULT);

        writeByte(4);
    }
}
