package dev.jaczerob.delfino.mrds.grpc;

import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.grpc.proto.account.AccountRequest;
import dev.jaczerob.delfino.grpc.proto.account.AccountResponse;
import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.Character;
import dev.jaczerob.delfino.grpc.proto.character.Equipment;
import dev.jaczerob.delfino.mrds.repositories.account.AccountEntity;
import dev.jaczerob.delfino.mrds.repositories.account.AccountRepository;
import dev.jaczerob.delfino.mrds.repositories.characters.CharacterEntity;
import dev.jaczerob.delfino.mrds.repositories.characters.CharacterRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AccountService extends AccountServiceGrpc.AccountServiceImplBase {
    private final AccountRepository accountRepository;
    private final CharacterRepository characterRepository;

    public AccountService(
            final AccountRepository accountRepository,
            final CharacterRepository characterRepository
    ) {
        this.accountRepository = accountRepository;
        this.characterRepository = characterRepository;
    }

    @Override
    public void getAccount(
            final AccountRequest request,
            final StreamObserver<AccountResponse> responseObserver
    ) {
        final var optionalAccount = this.accountRepository.findByUsername(request.getUsername());
        if (optionalAccount.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Account is not found").asRuntimeException());
            return;
        }

        final var account = optionalAccount.get();

        final var characters = this.characterRepository.findByAccountID(account.getId()).stream()
                .map(this::characterToProto)
                .toList();

        final var accountResponse = AccountResponse.newBuilder()
                .setAccount(this.fromEntity(account, characters))
                .build();

        responseObserver.onNext(accountResponse);
        responseObserver.onCompleted();
    }

    private Account fromEntity(final AccountEntity accountEntity, final List<Character> characters) {
        return Account.newBuilder()
                .setId(accountEntity.getId())
                .setName(accountEntity.getName())
                .setPassword(accountEntity.getPassword())
                .setPic(accountEntity.getPic())
                .setPin(accountEntity.getPin())
                .setGm(accountEntity.getGm())
                .addAllCharacters(characters)
                .build();
    }

    private Character characterToProto(final CharacterEntity character) {
        final var petEquips = Collections.nCopies(3, Equipment.newBuilder().setPosition(0).setId(0).build());

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
