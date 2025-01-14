package org.example.controller;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.example.model.InvertedIndexStorer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

public class StoreInvertedIndex implements InvertedIndexStorer {

    private static final Logger logger = Logger.getLogger(StoreInvertedIndex.class.getName());

    private final String DB_NAME = "BooksDatabase";
    private final String COLLECTION_NAME = "InvertedIndex";

    private static final int THREAD_COUNT = 4; // Adjust based on your system's cores

    public StoreInvertedIndex() {
        setupLogger();
    }

    @Override
    public void storeInvertedIndexJson(Map<String, List<Document>> invertedDict, String outputFolderPath) {
        File baseFolder = new File(outputFolderPath, "jsonDatamart");
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
            logger.info("Created base folder: " + baseFolder.getAbsolutePath());
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        int totalWords = invertedDict.size();

        for (Map.Entry<String, List<Document>> entry : invertedDict.entrySet()) {
            executor.submit(() -> {
                String word = entry.getKey();
                List<Document> metadataList = entry.getValue();

                File subFolder = new File(baseFolder, getSubfolderName(word));
                if (!subFolder.exists()) {
                    subFolder.mkdirs();
                    logger.info("Created subfolder: " + subFolder.getAbsolutePath());
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(subFolder, word + ".json")))) {
                    JSONObject jsonWordObject = new JSONObject().put(word, new JSONArray(metadataList));
                    writer.write(jsonWordObject.toString(4));
                } catch (IOException e) {
                    logger.severe("Error writing JSON file for word: " + word + " - " + e.getMessage());
                }
            });
        }

        executor.shutdown();
        logger.info("Completed storing inverted index as JSON files. Total words processed: " + totalWords);
    }

    @Override
    public void storeInvertedIndexMongo(Map<String, List<Document>> invertedDict) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = mongoClient.getDatabase(DB_NAME);
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);
            logger.info("Connected to MongoDB database: " + DB_NAME + ", collection: " + COLLECTION_NAME);

            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            int totalWords = invertedDict.size();

            for (Map.Entry<String, List<Document>> entry : invertedDict.entrySet()) {
                executor.submit(() -> {
                    String word = entry.getKey();
                    List<Document> books = entry.getValue();

                    String language = books.get(0).getString("language");
                    if (language == null) {
                        logger.warning("Language not found for word: " + word);
                        return;
                    }

                    Document filter = new Document("_id", language);
                    Document update = new Document("$addToSet", new Document("words", new Document("word", word).append("metadata", books)));

                    collection.updateOne(filter, update, new UpdateOptions().upsert(true));
                });
            }

            executor.shutdown();
            logger.info("Completed storing inverted index in MongoDB. Total words processed: " + totalWords);
        } catch (Exception e) {
            logger.severe("Error storing the inverted index in MongoDB: " + e.getMessage());
            throw e;
        }
    }

    private String getSubfolderName(String word) {
        char firstChar = Character.toUpperCase(word.charAt(0));
        if (firstChar >= 'A' && firstChar <= 'D') return "A-D";
        else if (firstChar >= 'E' && firstChar <= 'H') return "E-H";
        else if (firstChar >= 'I' && firstChar <= 'L') return "I-L";
        else if (firstChar >= 'M' && firstChar <= 'P') return "M-P";
        else if (firstChar >= 'Q' && firstChar <= 'T') return "Q-T";
        else if (firstChar >= 'U' && firstChar <= 'Z') return "U-Z";
        else return "Other";
    }

    private void setupLogger() {
        try {
            // Remove duplicate handlers
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // Configure the FileHandler to save logs to a file
            FileHandler fileHandler = new FileHandler("store_inverted_index.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);

            // Configure the ConsoleHandler to print logs to the console
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.ALL);
            logger.addHandler(consoleHandler);

            // Set log levels
            logger.setLevel(Level.ALL);
            rootLogger.setLevel(Level.ALL);

        } catch (IOException e) {
            logger.severe("Failed to set up logger: " + e.getMessage());
        }
    }
}