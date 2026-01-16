package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.client.SkillMacro;
import dev.jaczerob.delfino.maplestory.client.autoban.AutobanFactory;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

@Component
public final class SkillMacroHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.SKILL_MACRO;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        Character chr = client.getPlayer();
        int num = packet.readByte();
        if (num > 5) {
            return;
        }

        for (int i = 0; i < num; i++) {
            String name = packet.readString();
            if (name.length() > 12) {
                AutobanFactory.PACKET_EDIT.alert(chr, "Invalid name length " + name + " (" + name.length() + ") for skill macro.");
                client.disconnect(false, false);
                break;
            }

            int shout = packet.readByte();
            int skill1 = packet.readInt();
            int skill2 = packet.readInt();
            int skill3 = packet.readInt();
            SkillMacro macro = new SkillMacro(skill1, skill2, skill3, name, shout, i);
            chr.updateMacros(i, macro);
        }
    }
}
