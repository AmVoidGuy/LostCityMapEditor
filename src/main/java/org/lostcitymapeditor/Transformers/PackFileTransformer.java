package org.lostcitymapeditor.Transformers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PackFileTransformer {

    static String floPackPath = "Data/PackFiles/flo.pack";

    public static Map<Integer, String> parseFloPack() {
        Map<Integer, String> floMap = new HashMap<>();

        try (Scanner scanner = new Scanner(new File(floPackPath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        floMap.put(id, "[" + name + "]");
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid flo.pack line: " + line);
                    }
                } else {
                    System.err.println("Skipping invalid flo.pack line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading flo.pack file: " + e.getMessage());
        }

        return floMap;
    }

}
