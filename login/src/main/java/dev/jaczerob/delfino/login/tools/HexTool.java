package dev.jaczerob.delfino.login.tools;

import java.util.HexFormat;

public class HexTool {
    public static String toCompactHexString(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }
}
