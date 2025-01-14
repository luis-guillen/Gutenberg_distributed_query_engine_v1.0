package org.example.controller;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.example.utils.MetadataExtraction;
import org.example.model.InvertedIndexBuilder;

public class BuildInvertedIndex implements InvertedIndexBuilder {

    private static final Logger logger = Logger.getLogger(BuildInvertedIndex.class.getName());
    private final MetadataExtraction metadataExtraction = new MetadataExtraction();
    private static final CharArraySet stopWords = (CharArraySet) StandardAnalyzer.STOP_WORDS_SET;

    private static final int THREAD_COUNT = 4; // Number of threads for parallel processing

    static {
        setupLogger();
    }

    @Override
    public Map<String, List<Document>> buildInvertedIndex(String datalake) {
        // Use ConcurrentHashMap for thread-safe operations
        Map<String, List<Document>> invertedIndex = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        logger.info("Starting to build the inverted index from datalake: " + datalake);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(datalake), "*.txt")) {
            List<Path> files = new ArrayList<>();
            directoryStream.forEach(files::add); // Collect all files to process

            files.forEach(path -> executor.submit(() -> processFile(path, invertedIndex)));

        } catch (IOException e) {
            logger.severe("Error reading datalake path: " + e.getMessage());
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                logger.warning("Some tasks were forcibly terminated due to timeout.");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            logger.severe("Execution interrupted: " + e.getMessage());
        }

        logger.info("Finished building the inverted index for datalake: " + datalake);
        return invertedIndex;
    }

    private void processFile(Path path, Map<String, List<Document>> invertedIndex) {
        logger.info("Processing file: " + path.getFileName());

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 0;
            Map<String, String> metadata = metadataExtraction.extractMetadata(Files.readString(path));
            logger.fine("Extracted metadata from " + path.getFileName() + ": " + metadata);

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] words = line.toLowerCase().split("\\W+");

                for (String word : words) {
                    if (!stopWords.contains(word) && word.matches("^[a-zA-Z].*") && word.length() > 1) {
                        invertedIndex.computeIfAbsent(word, k -> Collections.synchronizedList(new ArrayList<>()));

                        Document wordEntry = new Document(metadata)
                                .append("line_number", lineNumber)
                                .append("line_text", line.trim());

                        synchronized (invertedIndex.get(word)) {
                            if (!invertedIndex.get(word).contains(wordEntry)) {
                                invertedIndex.get(word).add(wordEntry);
                            }
                        }
                    }
                }
            }

            logger.info("Finished processing file: " + path.getFileName());
        } catch (IOException e) {
            logger.severe("Error processing file " + path.getFileName() + ": " + e.getMessage());
        }
    }

    private static void setupLogger() {
        try {
            // Remove duplicate handlers
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }

            // Configure the FileHandler to save logs to a file
            FileHandler fileHandler = new FileHandler("built_inverted_index.log", true);
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