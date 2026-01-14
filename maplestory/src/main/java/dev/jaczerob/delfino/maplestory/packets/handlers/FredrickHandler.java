package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.processor.npc.FredrickProcessor;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public class FredrickHandler extends AbstractPacketHandler {
    private final FredrickProcessor fredrickProcessor;

    public FredrickHandler(FredrickProcessor fredrickProcessor) {
        this.fredrickProcessor = fredrickProcessor;
    }

    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.FREDRICK_ACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        byte operation = packet.readByte();

        switch (operation) {
            case 0x19: //Will never come...
                //client.sendPacket(PacketCreator.getFredrick((byte) 0x24));
                break;
            case 0x1A:
                fredrickProcessor.fredrickRetrieveItems(client);
                break;
            case 0x1C: //Exit
                break;
            default:
        }
    }
}
