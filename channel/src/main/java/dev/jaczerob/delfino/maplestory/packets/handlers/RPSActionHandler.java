package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.constants.id.NpcId;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.server.minigame.RockPaperScissor;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;


@Component
public final class RPSActionHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.RPS_ACTION;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        RockPaperScissor rps = chr.getRPS();

        if (client.tryacquireClient()) {
            try {
                if (packet.available() == 0 || !chr.getMap().containsNPC(NpcId.RPS_ADMIN)) {
                    if (rps != null) {
                        rps.dispose(client);
                    }
                    return;
                }
                final byte mode = packet.readByte();
                switch (mode) {
                    case 0: // start game
                    case 5: // retry
                        if (rps != null) {
                            rps.reward(client);
                        }
                        if (chr.getMeso() >= 1000) {
                            chr.setRPS(new RockPaperScissor(client, mode));
                        } else {
                            client.sendPacket(ChannelPacketCreator.getInstance().rpsMesoError(-1));
                        }
                        break;
                    case 1: // answer
                        if (rps == null || !rps.answer(client, packet.readByte())) {
                            client.sendPacket(ChannelPacketCreator.getInstance().rpsMode((byte) 0x0D));// 13
                        }
                        break;
                    case 2: // time over
                        if (rps == null || !rps.timeOut(client)) {
                            client.sendPacket(ChannelPacketCreator.getInstance().rpsMode((byte) 0x0D));
                        }
                        break;
                    case 3: // continue
                        if (rps == null || !rps.nextRound(client)) {
                            client.sendPacket(ChannelPacketCreator.getInstance().rpsMode((byte) 0x0D));
                        }
                        break;
                    case 4: // leave
                        if (rps != null) {
                            rps.dispose(client);
                        } else {
                            client.sendPacket(ChannelPacketCreator.getInstance().rpsMode((byte) 0x0D));
                        }
                        break;
                }
            } finally {
                client.releaseClient();
            }
        }
    }
}
