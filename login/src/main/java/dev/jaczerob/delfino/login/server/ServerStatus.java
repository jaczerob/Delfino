package dev.jaczerob.delfino.login.server;

public enum ServerStatus {
    NORMAL(0),
    BUSY(1),
    FULL(2);

    private final int code;

    ServerStatus(final int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}