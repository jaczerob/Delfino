package dev.jaczerob.delfino.login.packets.handlers;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.login.tools.LoginPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.springframework.stereotype.Component;

@Component
public class CustomPacketHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.CUSTOM_PACKET;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {
        if (p.available() > 0 && c.getGmLevel() == 4) {
            c.sendPacket(LoginPacketCreator.getInstance().customPacket(p.readBytes(p.available())));
        }
    }

    @Override
    public boolean validateState(final LoginClient c) {
        return true;
    }
}
