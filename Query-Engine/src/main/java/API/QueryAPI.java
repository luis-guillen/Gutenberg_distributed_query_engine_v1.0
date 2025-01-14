package API;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;

import java.util.*;
import java.util.stream.Collectors;


public class QueryAPI {
    private final HazelcastInstance hazelCast;
    private final int port;


    public QueryAPI(HazelcastInstance hazelCast, int port) {
        this.hazelCast = hazelCast;
        this.port = port;
    }


    public void startServer() {
        // Set the port for the server
        port(this.port);

        // Define the /search endpoint
        get("/search", (req, res) -> {
            String wordParam = req.queryParams("word");
            if (wordParam == null || wordParam.isEmpty()) {
                res.status(400);
                return "Parameter 'word' is required";
            }

            Map<String, Object> results = processSearch(wordParam);

            res.type("application/json");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(results);
        });

        // Define the /statistics endpoint
        get("/statistics", (req, res) -> {
            Map<String, List<String>> datalakeMap = hazelCast.getMap("datalake-map");

            int totalBooks = datalakeMap.values().stream()
                    .mapToInt(List::size)
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("Total books", totalBooks);

            res.type("application/json");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(stats);
        });
    }


    private Map<String, Object> processSearch(String wordParam) {
        List<String> words = Arrays.stream(wordParam.split("\\s+|\\+"))
                .map(String::trim)
                .collect(Collectors.toList());

        Map<String, Object> results = new HashMap<>();
        Map<String, String> hazelcastMap = hazelCast.getMap("datamart-map");

        for (String word : words) {
            if (hazelcastMap.containsKey(word)) {
                results.put(word, hazelcastMap.get(word));
            } else {
                results.put(word, "Not found");
            }
        }
        return results;
    }
}