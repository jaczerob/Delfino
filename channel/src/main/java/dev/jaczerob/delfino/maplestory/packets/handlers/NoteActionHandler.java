package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.model.Note;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.service.NoteService;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public final class NoteActionHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(NoteActionHandler.class);

    private final NoteService noteService;

    public NoteActionHandler(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.NOTE_ACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        int action = packet.readByte();
        if (action == 0 && client.getPlayer().getCashShop().getAvailableNotes() > 0) { // Reply to gift in cash shop
            String charname = packet.readString();
            String message = packet.readString();
            if (client.getPlayer().getCashShop().isOpened()) {
                context.writeAndFlush(ChannelPacketCreator.getInstance().showCashInventory(client));
            }

            boolean sendNoteSuccess = noteService.sendWithFame(message, client.getPlayer().getName(), charname);
            if (sendNoteSuccess) {
                client.getPlayer().getCashShop().decreaseNotes();
            }
        } else if (action == 1) { // Discard notes in game
            int num = packet.readByte();
            packet.readByte();
            packet.readByte();
            int fame = 0;
            for (int i = 0; i < num; i++) {
                int id = packet.readInt();
                packet.readByte(); //Fame, but we read it from the database :)

                Optional<Note> discardedNote = noteService.delete(id);
                if (discardedNote.isEmpty()) {
                    log.warn("Note with id {} not able to be discarded. Already discarded?", id);
                    continue;
                }

                fame += discardedNote.get().fame();
            }
            if (fame > 0) {
                client.getPlayer().gainFame(fame);
            }
        }
    }
}
