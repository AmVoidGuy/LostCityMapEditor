package org.lostcitymapeditor.Util;

import java.util.HashMap;
import java.util.Map;

public class DataHelpers {
    static Map<Integer, Integer> modelToLocMap = new HashMap<>();

    public static Integer parseInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
