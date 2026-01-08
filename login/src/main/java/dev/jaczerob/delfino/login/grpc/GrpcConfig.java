package dev.jaczerob.delfino.login.grpc;

import dev.jaczerob.delfino.grpc.proto.WorldServiceGrpc;
import dev.jaczerob.delfino.grpc.proto.character.CharacterServiceGrpc;
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
    public CharacterServiceGrpc.CharacterServiceBlockingV2Stub characterServiceStub(final GrpcChannelFactory grpcChannelFactory) {
        return CharacterServiceGrpc.newBlockingV2Stub(
                grpcChannelFactory.createChannel("0.0.0.0:9090")
        );
    }
}
