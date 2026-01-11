package dev.jaczerob.delfino.mrds.grpc;

import dev.jaczerob.delfino.grpc.proto.account.Account;
import dev.jaczerob.delfino.grpc.proto.account.AccountRequest;
import dev.jaczerob.delfino.grpc.proto.account.AccountResponse;
import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import dev.jaczerob.delfino.mrds.repositories.account.AccountEntity;
import dev.jaczerob.delfino.mrds.repositories.account.AccountRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class AccountService extends AccountServiceGrpc.AccountServiceImplBase {
    private final AccountRepository accountRepository;

    public AccountService(final AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void getAccount(
            final AccountRequest request,
            final StreamObserver<AccountResponse> responseObserver
    ) {
        final var account = this.accountRepository.findByUsername(request.getUsername());
        if (account.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND.withDescription("Account is not found").asRuntimeException());
            return;
        }

        final var accountResponse = AccountResponse.newBuilder()
                .setAccount(this.fromEntity(account.get()))
                .build();

        responseObserver.onNext(accountResponse);
        responseObserver.onCompleted();
    }

    private Account fromEntity(final AccountEntity accountEntity) {
        return Account.newBuilder()
                .setId(accountEntity.getId())
                .setName(accountEntity.getUsername())
                .setPassword(accountEntity.getPassword())
                .setPic(accountEntity.getPic())
                .setPin(accountEntity.getPin())
                .setGm(accountEntity.getGm())
                .build();
    }
}
