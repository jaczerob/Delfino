package dev.jaczerob.delfino.maplestory.net;

import dev.jaczerob.delfino.maplestory.service.NoteService;

import java.util.Objects;

public record ChannelDependencies(NoteService noteService) {

    public ChannelDependencies {
        Objects.requireNonNull(noteService);
    }
}
