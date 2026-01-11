package dev.jaczerob.delfino.network.tools;

import java.util.HexFormat;

public class HexTool {
    public static String toHexString(byte[] bytes) {
        return HexFormat.ofDelimiter(" ").withUpperCase().formatHex(bytes);
    }

    public static String toStringFromAscii(final byte[] bytes) {
        final var filteredBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            if (isSpecialCharacter(bytes[i])) {
                filteredBytes[i] = '.';
            } else {
                filteredBytes[i] = (byte) (bytes[i] & 0xFF);
            }
        }

        return new String(filteredBytes);
    }

    private static boolean isSpecialCharacter(byte asciiCode) {
        return asciiCode >= 0 && asciiCode <= 31;
    }
}
