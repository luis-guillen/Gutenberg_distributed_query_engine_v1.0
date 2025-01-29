package Controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordList {

    private final String folderRangePath;

    public WordList(String folderRangePath) {
        this.folderRangePath = folderRangePath;
    }

    public List<Map<String, String>> wordMapCreator() {
        List<Map<String, String>> wordsList = new ArrayList<>();
        File folder = new File(folderRangePath);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));

            if (files != null) {
                for (File file : files) {
                    String word = file.getName().replace(".json", ""); // Extract the word from the file name

                    try {
                        String content = Files.readString(file.toPath()); // Read file content

                        // Parse the JSON as a map
                        Map<String, String> wordMap = new HashMap<>();

                        // Add to the main map
                        wordMap.put(word, content);
                        wordsList.add(wordMap);

                    } catch (IOException e) {
                        System.err.println("Error reading or parsing the file: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("Directory not found: " + folderRangePath);
        }

        return wordsList;
    }
}