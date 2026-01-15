package dev.jaczerob.delfino.elm.grpc;

import dev.jaczerob.delfino.common.config.DelfinoConfigurationProperties;
import dev.jaczerob.delfino.grpc.proto.account.AccountServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcConfig {
    @Bean
    public AccountServiceGrpc.AccountServiceBlockingV2Stub accountServiceStub(
            final GrpcChannelFactory grpcChannelFactory,
            final DelfinoConfigurationProperties delfinoConfigurationProperties
    ) {
        return AccountServiceGrpc.newBlockingV2Stub(
                grpcChannelFactory.createChannel(delfinoConfigurationProperties.getMdrs().getUrl())
        );
    }
}
