package dev.jaczerob.delfino.elm.utils;

public class StringUtils {
    public static String getRightPaddedStr(final String in, final char padchar, final int length) {
        return in + String.valueOf(padchar).repeat(Math.max(0, length - in.length()));
    }
}
