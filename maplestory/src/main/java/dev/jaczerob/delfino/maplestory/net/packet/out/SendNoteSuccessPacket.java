package dev.jaczerob.delfino.network.packets.out;

import dev.jaczerob.delfino.maplestory.net.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.ByteBufOutPacket;

public class SendNoteSuccessPacket extends ByteBufOutPacket {

    public SendNoteSuccessPacket() {
        super(SendOpcode.MEMO_RESULT);

        writeByte(4);
    }
}
