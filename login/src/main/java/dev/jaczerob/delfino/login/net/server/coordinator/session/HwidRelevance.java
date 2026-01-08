package dev.jaczerob.delfino.login.net.server.coordinator.session;

public record HwidRelevance(String hwid, int relevance) {
    public int getIncrementedRelevance() {
        return relevance < Byte.MAX_VALUE ? relevance + 1 : relevance;
    }
}
