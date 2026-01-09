package dev.jaczerob.delfino.maplestory.grpc;

import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.grpc.proto.character.CharacterServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.CharactersRequest;
import dev.jaczerob.delfino.grpc.proto.character.CharactersResponse;
import dev.jaczerob.delfino.grpc.proto.character.Equipment;
import dev.jaczerob.delfino.maplestory.client.inventory.Item;
import dev.jaczerob.delfino.maplestory.client.inventory.ItemFactory;
import dev.jaczerob.delfino.maplestory.repositories.CharacterEntity;
import dev.jaczerob.delfino.maplestory.repositories.CharacterRepository;
import dev.jaczerob.delfino.maplestory.tools.Pair;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class CharacterService extends CharacterServiceGrpc.CharacterServiceImplBase {
    private final CharacterRepository characterRepository;

    public CharacterService(final CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    @Override
    public void getCharacters(final CharactersRequest request, final StreamObserver<CharactersResponse> responseObserver) {
        final var characters = this.characterRepository.findByAccountid(request.getAccountId()).stream()
                .map(this::characterToProto)
                .toList();

        final var responseBuilder = CharactersResponse.newBuilder()
                .addAllCharacters(characters)
                .build();

        responseObserver.onNext(responseBuilder);
        responseObserver.onCompleted();
    }

    private Character characterToProto(final CharacterEntity character) {
        final List<Item> equippedItems;

        try {
            equippedItems = ItemFactory.loadEquippedItems(character.getId(), true, true).stream()
                    .map(Pair::getLeft)
                    .toList();
        } catch (final SQLException sqlException) {
            throw new IllegalStateException("Failed to load equipped items for character id " + character.getId(), sqlException);
        }

        final var normalEquips = new LinkedHashMap<Integer, Equipment>();
        final var maskedEquips = new LinkedHashMap<Integer, Equipment>();
        for (final var item : equippedItems) {
            final var pos = item.getPosition() * -1;
            if (pos < 100 && normalEquips.get(pos) == null) {
                normalEquips.put(pos, this.itemToProto(item));
            } else if (pos > 100 && pos != 111) {
                final var offsetPos = pos - 100;
                if (normalEquips.get(offsetPos) != null) {
                    maskedEquips.put(offsetPos, normalEquips.get(pos));
                } else {
                    normalEquips.put(pos, this.itemToProto(item));
                }
            } else if (normalEquips.get(pos) != null) {
                maskedEquips.put(pos, this.itemToProto(item));
            }
        }

        final int weaponId;
        if (normalEquips.containsKey(-111)) {
            weaponId = normalEquips.get(-111).getId();
        } else if (maskedEquips.containsKey(-111)) {
            weaponId = maskedEquips.get(-111).getId();
        } else {
            weaponId = 0;
        }

        final var petEquips = IntStream.range(0, 3)
                .mapToObj(ignored -> Equipment.newBuilder().setPosition(0).setId(0).build())
                .toList();

        return Character.newBuilder()
                .setId(character.getId())
                .setName(character.getName())
                .setGender(character.getGender())
                .setSkinColor(character.getSkincolor())
                .setFace(character.getFace())
                .setHair(character.getHair())
                .addAllPets(List.of(0, 0, 0))
                .setPets(0, 0)
                .setPets(0, 0)
                .setLevel(character.getLevel())
                .setJob(character.getJob())
                .setStr(character.getStr())
                .setDex(character.getDex())
                .setInt(character.getInt_())
                .setLuk(character.getLuk())
                .setHp(character.getHp())
                .setMp(character.getMp())
                .setMaxHp(character.getMaxhp())
                .setMaxMp(character.getMaxmp())
                .setRemainingAp(0)
                .setRemainingSp(0)
                .setExp(character.getExp())
                .setFame(character.getFame())
                .setGachaExp(character.getGachaexp())
                .setMapId(character.getMap())
                .setSpawnPoint(character.getSpawnpoint())
                .addAllEquipment(normalEquips.sequencedValues())
                .addAllMaskedEquipment(maskedEquips.sequencedValues())
                .setWeapon(weaponId)
                .setRank(character.getRank())
                .setRankMove(character.getRankMove())
                .setJobRank(character.getJobRank())
                .setJobRankMove(character.getJobRankMove())
                .setGmLevel(character.getGm())
                .addAllPetEquipment(petEquips)
                .build();
    }

    private Equipment itemToProto(final dev.jaczerob.delfino.maplestory.client.inventory.Item item) {
        return Equipment.newBuilder()
                .setId(item.getItemId())
                .setPosition(item.getPosition())
                .build();
    }
}
