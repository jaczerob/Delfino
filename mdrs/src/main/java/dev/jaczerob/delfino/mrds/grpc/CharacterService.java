package dev.jaczerob.delfino.mrds.grpc;

import dev.jaczerob.delfino.grpc.proto.character.*;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.mrds.repositories.characters.CharacterEntity;
import dev.jaczerob.delfino.mrds.repositories.characters.CharacterRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

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
        final var characters = this.characterRepository.findByAccountID(request.getAccountId()).stream()
                .map(this::characterToProto)
                .toList();

        final var responseBuilder = CharactersResponse.newBuilder()
                .addAllCharacters(characters)
                .build();

        responseObserver.onNext(responseBuilder);
        responseObserver.onCompleted();
    }

    private Character characterToProto(final CharacterEntity character) {
        final var petEquips = IntStream.range(0, 3)
                .mapToObj(ignored -> Equipment.newBuilder().setPosition(0).setId(0).build())
                .toList();

        return Character.newBuilder()
                .setId(character.getId())
                .setName(character.getName())
                .setGender(character.getGender())
                .setSkinColor(character.getSkinColor())
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
                .setMaxHp(character.getMaxHP())
                .setMaxMp(character.getMaxMP())
                .setRemainingAp(0)
                .setRemainingSp(0)
                .setExp(character.getExp())
                .setFame(character.getFame())
                .setGachaExp(character.getGachaEXP())
                .setMapId(character.getMap())
                .setSpawnPoint(character.getSpawnPoint())
                .addAllEquipment(List.of())
                .addAllMaskedEquipment(List.of())
                .setWeapon(0)
                .setRank(character.getRank())
                .setRankMove(character.getRankMove())
                .setJobRank(character.getJobRank())
                .setJobRankMove(character.getJobRankMove())
                .setGmLevel(character.getGm())
                .addAllPetEquipment(petEquips)
                .build();
    }
}
