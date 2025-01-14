package org.example.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataExtraction {

    // Patterns are compiled once for reuse across multiple threads
    private static final Pattern TITLE_PATTERN = Pattern.compile("Title:\\s*(.*)");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("Author:\\s*(.*)");
    private static final Pattern RELEASE_DATE_PATTERN = Pattern.compile("Release date:\\s*(.*)");
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("Language:\\s*(.*)");


    public Map<String, String> extractMetadata(String content) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("title", extractMatch(content, TITLE_PATTERN));
        metadata.put("author", extractMatch(content, AUTHOR_PATTERN));
        metadata.put("release_date", extractMatch(content, RELEASE_DATE_PATTERN));
        metadata.put("language", extractMatch(content, LANGUAGE_PATTERN));
        return metadata;
    }

    private static String extractMatch(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1) : "Unknown";
    }
}