package dev.jaczerob.delfino.maplestory.grpc;

import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.grpc.proto.character.CharacterServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.CharactersRequest;
import dev.jaczerob.delfino.grpc.proto.character.CharactersResponse;
import dev.jaczerob.delfino.grpc.proto.character.Equipment;
import dev.jaczerob.delfino.maplestory.repositories.CharacterEntity;
import dev.jaczerob.delfino.maplestory.repositories.CharacterRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .setWeapon(Equipment.newBuilder().setId(0).setPosition(0).build())
                .setRank(character.getRank())
                .setRankMove(character.getRankMove())
                .setJobRank(character.getJobRank())
                .setJobRankMove(character.getJobRankMove())
                .setGmLevel(character.getGm())
                .build();
    }
}
