package dev.jaczerob.delfino.login.tools;

public class StringUtil {
    public static String getRightPaddedStr(final String in, final char padchar, final int length) {
        return in + String.valueOf(padchar).repeat(Math.max(0, length - in.length()));
    }
}