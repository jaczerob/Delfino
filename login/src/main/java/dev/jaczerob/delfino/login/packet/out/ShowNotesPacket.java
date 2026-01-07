package dev.jaczerob.delfino.login.packet.out;

import dev.jaczerob.delfino.login.model.Note;
import dev.jaczerob.delfino.login.opcodes.SendOpcode;
import dev.jaczerob.delfino.login.packet.ByteBufOutPacket;

import java.util.List;
import java.util.Objects;

import static dev.jaczerob.delfino.login.tools.PacketCreator.getTime;

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
        writeLong(getTime(note.timestamp()));
        writeByte(note.fame());
    }
}
