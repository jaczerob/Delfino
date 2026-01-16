package dev.jaczerob.delfino.elm.events.loggedin.characterlistrequest;

import dev.jaczerob.delfino.elm.client.Client;
import dev.jaczerob.delfino.elm.coordinators.SessionCoordinator;
import dev.jaczerob.delfino.elm.events.loggedin.AbstractLoggedInEventHandler;
import dev.jaczerob.delfino.elm.utils.StringUtils;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.network.opcodes.SendOpcode;
import dev.jaczerob.delfino.network.packets.OutPacket;
import dev.jaczerob.delfino.network.packets.Packet;
import org.springframework.stereotype.Component;

@Component
public class CharacterListRequestEventHandler extends AbstractLoggedInEventHandler<Object, CharacterListRequestEvent> {
    public CharacterListRequestEventHandler(SessionCoordinator sessionCoordinator) {
        super(sessionCoordinator);
    }

    @Override
    protected void handleEventInternal(final CharacterListRequestEvent event) {
        event.getContext().writeAndFlush(this.createPacket(event.getClient()));
    }

    private Packet createPacket(final Client client) {
        final var packet = OutPacket.create(SendOpcode.CHARLIST)
                .writeByte(0);

        final var chars = client.getAccount().getCharactersList();
        packet.writeByte((byte) chars.size());

        for (final var character : chars) {
            this.addCharEntry(packet, character, false);
        }

        return packet.writeByte(1)
                .writeInt(3 - client.getAccount().getCharactersCount() + 1);
    }

    private void addCharEntry(final OutPacket packet, final Character character, final boolean viewAll) {
        this.addCharStats(packet, character);
        this.addCharLook(packet, character, false);
        if (!viewAll) {
            packet.writeByte(0);
        }

        if (character.getGmLevel() > 0) {
            packet.writeByte(0);
            return;
        }

        packet.writeByte(1);
        packet.writeInt(character.getRank());
        packet.writeInt(character.getRankMove());
        packet.writeInt(character.getJobRank());
        packet.writeInt(character.getJobRankMove());
    }

    private void addCharStats(final OutPacket packet, final Character character) {
        packet.writeInt(character.getId());
        packet.writeFixedString(StringUtils.getRightPaddedStr(character.getName(), '\0', 13));
        packet.writeByte(character.getGender());
        packet.writeByte(character.getSkinColor());
        packet.writeInt(character.getFace());
        packet.writeInt(character.getHair());

        for (final var pet : character.getPetsList()) {
            packet.writeLong(pet);
        }

        packet.writeByte(character.getLevel());
        packet.writeShort(character.getJob());
        packet.writeShort(character.getStr());
        packet.writeShort(character.getDex());
        packet.writeShort(character.getInt());
        packet.writeShort(character.getLuk());
        packet.writeShort(character.getHp());
        packet.writeShort(character.getMaxHp());
        packet.writeShort(character.getMp());
        packet.writeShort(character.getMaxMp());
        packet.writeShort(character.getRemainingAp());
        packet.writeShort(character.getRemainingSp());
        packet.writeInt(character.getExp());
        packet.writeShort(character.getFame());
        packet.writeInt(character.getGachaExp());
        packet.writeInt(character.getMapId());
        packet.writeByte(character.getSpawnPoint());
        packet.writeInt(0);
    }

    private void addCharLook(final OutPacket packet, final Character character, final boolean mega) {
        packet.writeByte(character.getGender());
        packet.writeByte(character.getSkinColor());
        packet.writeInt(character.getFace());
        packet.writeBool(!mega);
        packet.writeInt(character.getHair());
        addCharEquips(packet, character);
    }

    private void addCharEquips(final OutPacket packet, final Character character) {
        for (final var equip : character.getEquipmentList()) {
            packet.writeByte(Math.abs(equip.getPosition()));
            packet.writeInt(equip.getId());
        }

        packet.writeByte(0xFF);

        for (final var equip : character.getMaskedEquipmentList()) {
            packet.writeByte(Math.abs(equip.getPosition()));
            packet.writeInt(equip.getId());
        }

        packet.writeByte(0xFF);
        packet.writeInt(0);

        for (final var pet : character.getPetEquipmentList()) {
            packet.writeInt(pet.getId());
        }
    }
}
