package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.processor.npc.DueyProcessor;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class DueyHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.DUEY_ACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!YamlConfig.config.server.USE_DUEY) {
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
            return;
        }

        byte operation = packet.readByte();
        if (operation == DueyProcessor.Actions.TOSERVER_RECV_ITEM.getCode()) { // on click 'O' Button, thanks inhyuk
            DueyProcessor.dueySendTalk(client, false);
        } else if (operation == DueyProcessor.Actions.TOSERVER_SEND_ITEM.getCode()) {
            byte inventId = packet.readByte();
            short itemPos = packet.readShort();
            short amount = packet.readShort();
            int mesos = packet.readInt();
            String recipient = packet.readString();
            boolean quick = packet.readByte() != 0;
            String message = quick ? packet.readString() : null;

            DueyProcessor.dueySendItem(client, inventId, itemPos, amount, mesos, message, recipient, quick);
        } else if (operation == DueyProcessor.Actions.TOSERVER_REMOVE_PACKAGE.getCode()) {
            int packageid = packet.readInt();

            DueyProcessor.dueyRemovePackage(client, packageid, true);
        } else if (operation == DueyProcessor.Actions.TOSERVER_CLAIM_PACKAGE.getCode()) {
            int packageid = packet.readInt();

            DueyProcessor.dueyClaimPackage(client, packageid);
        } else if (operation == DueyProcessor.Actions.TOSERVER_CLAIM_PACKAGE.getCode()) {
            DueyProcessor.dueySendTalk(client, false);
        }
    }
}
