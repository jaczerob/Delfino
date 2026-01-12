package dev.jaczerob.delfino.login.grpc;

import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {
    @Bean
    public WorldServiceGrpc.WorldServiceBlockingV2Stub worldServiceStub(final GrpcChannelFactory grpcChannelFactory) {
        return WorldServiceGrpc.newBlockingV2Stub(
                grpcChannelFactory.createChannel("0.0.0.0:9090")
        );
    }

    @Bean
    public AccountServiceGrpc.AccountServiceBlockingV2Stub accountServiceStub(final GrpcChannelFactory grpcChannelFactory) {
        return AccountServiceGrpc.newBlockingV2Stub(
                grpcChannelFactory.createChannel("0.0.0.0:9090")
        );
    }
}
