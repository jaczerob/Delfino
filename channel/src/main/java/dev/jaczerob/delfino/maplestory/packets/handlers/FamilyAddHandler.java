package dev.jaczerob.delfino.maplestory.packets.handlers;

import dev.jaczerob.delfino.maplestory.client.Character;
import dev.jaczerob.delfino.maplestory.client.Client;
import dev.jaczerob.delfino.maplestory.config.YamlConfig;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator;
import dev.jaczerob.delfino.maplestory.net.server.coordinator.world.InviteCoordinator.InviteType;
import dev.jaczerob.delfino.maplestory.packets.AbstractPacketHandler;
import dev.jaczerob.delfino.maplestory.tools.ChannelPacketCreator;
import dev.jaczerob.delfino.network.opcodes.RecvOpcode;
import dev.jaczerob.delfino.network.packets.InPacket;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * @author Jay Estrella
 * @author Ubaware
 */
@Component
public final class FamilyAddHandler extends AbstractPacketHandler {
    @Override
    public RecvOpcode getOpcode() {
        return RecvOpcode.ADD_FAMILY;
    }

    @Override
    public void handlePacket(final InPacket packet, final Client client, final ChannelHandlerContext context) {
        if (!YamlConfig.config.server.USE_FAMILY_SYSTEM) {
            return;
        }
        String toAdd = packet.readString();
        Character addChr = client.getChannelServer().getPlayerStorage().getCharacterByName(toAdd);
        Character chr = client.getPlayer();
        if (addChr == null) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(65, 0));
        } else if (addChr == chr) { //only possible through packet editing/client editing i think?
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (addChr.getMap() != chr.getMap() || (addChr.isHidden()) && chr.gmLevel() < addChr.gmLevel()) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(69, 0));
        } else if (addChr.getLevel() <= 10) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(77, 0));
        } else if (Math.abs(addChr.getLevel() - chr.getLevel()) > 20) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(72, 0));
        } else if (addChr.getFamily() != null && addChr.getFamily() == chr.getFamily()) { //same family
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        } else if (InviteCoordinator.hasInvite(InviteType.FAMILY, addChr.getId())) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(73, 0));
        } else if (chr.getFamily() != null && addChr.getFamily() != null && addChr.getFamily().getTotalGenerations() + chr.getFamily().getTotalGenerations() > YamlConfig.config.server.FAMILY_MAX_GENERATIONS) {
            client.sendPacket(ChannelPacketCreator.getInstance().sendFamilyMessage(76, 0));
        } else {
            InviteCoordinator.createInvite(InviteType.FAMILY, chr, addChr, addChr.getId());
            addChr.getClient().sendPacket(ChannelPacketCreator.getInstance().sendFamilyInvite(chr.getId(), chr.getName()));
            chr.dropMessage("The invite has been sent.");
            client.sendPacket(ChannelPacketCreator.getInstance().enableActions());
        }
    }
}
