package org.lostcitymapeditor.Util;

public class DataHelpers {
    public static Integer parseInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
