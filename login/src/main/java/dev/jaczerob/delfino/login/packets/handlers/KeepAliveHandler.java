package dev.jaczerob.delfino.login.packets.handlers;

import dev.jaczerob.delfino.login.client.LoginClient;
import dev.jaczerob.delfino.login.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import org.springframework.stereotype.Component;

@Component
public class KeepAliveHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.PONG;
    }

    @Override
    public void handlePacket(final InPacket p, final LoginClient c) {

    }

    @Override
    public boolean validateState(final LoginClient c) {
        return true;
    }
}
