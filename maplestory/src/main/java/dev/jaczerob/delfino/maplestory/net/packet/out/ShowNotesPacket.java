package dev.jaczerob.delfino.network.packets.out;

import dev.jaczerob.delfino.maplestory.model.Note;
import dev.jaczerob.delfino.maplestory.net.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.ByteBufOutPacket;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;

import java.util.List;
import java.util.Objects;

public final class ShowNotesPacket extends ByteBufOutPacket {

    public ShowNotesPacket(List<Note> notes) {
        super(SendOpcode.MEMO_RESULT);
        Objects.requireNonNull(notes);

        writeByte(3);
        writeByte(notes.size());
        notes.forEach(this::writeNote);
    }

    private void writeNote(Note note) {
        writeInt(note.id());
        writeString(note.from() + " "); //Stupid nexon forgot space lol
        writeString(note.message());
        writeLong(ChannelPacketCreator.getInstance().getTime(note.timestamp()));
        writeByte(note.fame());
    }
}
