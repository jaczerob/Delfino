package dev.jaczerob.delfino.maplestory.tools;

import java.util.HexFormat;

public class HexTool {
    public static byte[] toBytes(String hexString) {
        return HexFormat.of().parseHex(removeAllSpaces(hexString));
    }

    private static String removeAllSpaces(String input) {
        return input.replaceAll("\\s", "");
    }
}
