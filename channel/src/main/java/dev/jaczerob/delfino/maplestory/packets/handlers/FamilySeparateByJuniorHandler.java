package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import org.springframework.stereotype.Component;

@Component
public class FamilySeparateByJuniorHandler extends FamilySeparateHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SEPARATE_FAMILY_BY_JUNIOR;
    }
}
