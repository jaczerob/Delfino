package dev.jaczerob.delfino.maplestory.tools;

import dev.jaczerob.delfino.maplestory.constants.string.CharsetConstants;

import java.util.HexFormat;

/**
 * Handles converting back and forth from byte arrays to hex strings.
 */
public class HexTool {

    /**
     * Convert a byte array to its hex string representation (upper case).
     * Each byte value is converted to two hex characters delimited by a space.
     *
     * @param bytes Byte array to convert to a hex string.
     *              Example: {1, 16, 127, -1} is converted to "01 F0 7F FF"
     * @return The hex string
     */
    public static String toHexString(byte[] bytes) {
        return HexFormat.ofDelimiter(" ").withUpperCase().formatHex(bytes);
    }

    /**
     * Convert a byte array to its hex string representation (upper case).
     * Like {@link #toHexString(byte[]) HexTool.toString}, but with no space delimiter.
     *
     * @return The compact hex string
     */
    public static String toCompactHexString(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

    /**
     * Convert a hex string to its byte array representation. Two consecutive hex characters are converted to one byte.
     *
     * @param hexString Hex string to convert to bytes. May be lower or upper case, and hex character pairs may be
     *                  delimited by a space or not.
     *                  Example: "01 10 7F FF" is converted to {1, 16, 127, -1}.
     *                  The following hex strings are considered identical and are converted to the same byte array:
     *                  "01 10 7F FF", "01107FFF", "01 10 7f ff", "01107fff"
     * @return The byte array
     */
    public static byte[] toBytes(String hexString) {
        return HexFormat.of().parseHex(removeAllSpaces(hexString));
    }

    private static String removeAllSpaces(String input) {
        return input.replaceAll("\\s", "");
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
