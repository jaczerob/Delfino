# World

This is the world server. This serves as an aggregate for channel data via gRPC.
It also hold the base configuration for all channel servers.

# gRPC

This service will call each individual channel server to get the data it needs as well as returning the
world server's internal state.

## World Service

### GetWorld

This will return a view of the world configuration and each channel's status and active playerbase.
It will call each channel server via gRPC to get the channel data. You can view the structure of all protos
in the grpc-proto module.
