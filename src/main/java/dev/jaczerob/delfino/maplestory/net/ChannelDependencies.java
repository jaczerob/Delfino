package dev.jaczerob.delfino.maplestory.net;

import dev.jaczerob.delfino.maplestory.client.processor.npc.FredrickProcessor;
import dev.jaczerob.delfino.maplestory.service.NoteService;

import java.util.Objects;

public record ChannelDependencies(NoteService noteService, FredrickProcessor fredrickProcessor) {

    public ChannelDependencies {
        Objects.requireNonNull(noteService);
        Objects.requireNonNull(fredrickProcessor);
    }
}
