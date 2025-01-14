package Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class BookList {

    public static Map<String, String> bookMapCreator(String datalakePath) {
        Map<String, String> bookMap = new HashMap<>();
        File folder = new File(datalakePath);

        // Verify if the path is valid
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().startsWith("book_")) {
                        String fileName = file.getName();
                        String uuid = fileName.substring(5); // Removes the "book_" prefix

                        // Add the ID and content to the map
                        bookMap.put(uuid, fileName);
                    }
                }
            }
        } else {
            System.err.println("The provided path is not a valid directory: " + datalakePath);
        }
        return bookMap;
    }
}