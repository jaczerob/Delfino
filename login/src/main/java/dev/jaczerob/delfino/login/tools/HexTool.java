package dev.jaczerob.delfino.login.tools;

import dev.jaczerob.delfino.login.constants.string.CharsetConstants;

import java.util.HexFormat;

public class HexTool {
    public static String toHexString(byte[] bytes) {
        return HexFormat.ofDelimiter(" ").withUpperCase().formatHex(bytes);
    }

    public static String toCompactHexString(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

    public static String toStringFromAscii(final byte[] bytes) {
        byte[] filteredBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            if (isSpecialCharacter(bytes[i])) {
                filteredBytes[i] = '.';
            } else {
                filteredBytes[i] = (byte) (bytes[i] & 0xFF);
            }
        }

        return new String(filteredBytes, CharsetConstants.CHARSET);
    }

    private static boolean isSpecialCharacter(byte asciiCode) {
        return asciiCode >= 0 && asciiCode <= 31;
    }
}
