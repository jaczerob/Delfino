package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.id.NpcId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.scripts.ScriptManager;
import dev.jaczerob.delfino.maplestory.scripts.ScriptType;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ClickGuideHandler extends AbstractPacketHandler {
    private final ScriptManager scriptManager;

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CLICK_GUIDE;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        this.scriptManager.runScript(String.valueOf(NpcId.LILIN), ScriptType.NPC);
    }
}
