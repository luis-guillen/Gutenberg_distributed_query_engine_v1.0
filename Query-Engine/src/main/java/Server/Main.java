package Server;

import API.QueryAPI;
import com.hazelcast.core.HazelcastInstance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static String datalakePath = "/data/datalake";
    private static String jsonDatamart = "/data/datamart";

    public static void main(String[] args) {
        HazelCastProcessor hazelCastProcessor = new HazelCastProcessor(datalakePath, jsonDatamart);
        HazelcastInstance hazelcastInstance = hazelCastProcessor.getHazelcastInstance();

        System.out.println("=== STARTING PARALLEL PROCESSES ===");

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.execute(() -> {
            System.out.println("=== PROCESSING DATALAKE ===");
            hazelCastProcessor.processData();
            System.out.println("=== DATALAKE PROCESSING COMPLETED ===");
        });

        try {
            Thread.sleep(10000); // Wait for 10 seconds
        } catch (InterruptedException e) {
            System.err.println("Error while waiting between threads: " + e.getMessage());
        }

        // 🚀 Execute loadData() on the main thread (not in parallel)
        System.out.println("=== PROCESSING DATAMART ===");
        hazelCastProcessor.loadData();
        System.out.println("=== DATAMART PROCESSING COMPLETED ===");

        // Wait for parallel tasks to finish before starting the API
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        int apiPort = 4567; // API port
        QueryAPI queryAPI = new QueryAPI(hazelcastInstance, apiPort);
        queryAPI.startServer(); // Should start the server correctly

        System.out.println("API started on port " + apiPort);

        System.out.println("\n=== FINALIZING ALL PROCESSES ===");
    }
}