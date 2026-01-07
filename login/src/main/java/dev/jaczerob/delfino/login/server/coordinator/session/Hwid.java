package dev.jaczerob.delfino.login.server.coordinator.session;

import java.util.regex.Pattern;

public record Hwid(String hwid) {
    private static final int HWID_LENGTH = 8;
    private static final Pattern VALID_HOST_STRING_PATTERN = Pattern.compile("[0-9A-F]{12}_[0-9A-F]{8}");

    private static boolean isValidHostString(String hostString) {
        return VALID_HOST_STRING_PATTERN.matcher(hostString).matches();
    }

    public static Hwid fromHostString(String hostString) throws IllegalArgumentException {
        if (hostString == null || !isValidHostString(hostString)) {
            throw new IllegalArgumentException("hostString has invalid format");
        }

        final var split = hostString.split("_");
        if (split.length != 2 || split[1].length() != HWID_LENGTH) {
            throw new IllegalArgumentException("Hwid validation failed for hwid: " + hostString);
        }

        return new Hwid(split[1]);
    }
}
