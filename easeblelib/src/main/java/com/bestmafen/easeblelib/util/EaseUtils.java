package com.bestmafen.easeblelib.util;

import java.util.Arrays;
import java.util.Locale;

public class EaseUtils {

    public static String byteArray2HexString(byte[] data) {
        if (data == null || data.length == 0) return "";

        String[] strings = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            strings[i] = String.format(Locale.getDefault(), "0x%02X", data[i]);
        }
        return Arrays.toString(strings);
    }
}
