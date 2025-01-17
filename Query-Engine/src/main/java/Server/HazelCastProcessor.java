package Server;

import Controller.BookList;
import Controller.WordList;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.map.IMap;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class HazelCastProcessor {
    private final String dataLakePath;
    private final String dataMartPath;
    private HazelcastInstance hazelcastInstance;

    public HazelCastProcessor(String dataLakePath, String dataMartPath) {
        this.dataLakePath = dataLakePath;
        this.dataMartPath = dataMartPath;

        // Crear configuración de Hazelcast
        Config config = new Config();

        // Establecer el nombre del clúster
        config.setClusterName("dev"); // Cambiar "dev" por el nombre del clúster deseado

        // Configurar la unión de nodos
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false); // Desactivar multicast
        joinConfig.getTcpIpConfig().setEnabled(true)      // Activar TCP/IP
                .addMember("172.20.10.11:5701")
                .addMember("172.20.10.10:5701")
                .addMember("172.20.10.12:5701");

        // Crear instancia de Hazelcast
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);
    }
    public void processData() {
        Map<String, List<String>> hazelcastMap = hazelcastInstance.getMap("datalake-map");

        Map<String, String> bookMap = BookList.bookMapCreator(dataLakePath);

        int batchSize = 50;  // Reduce batch size to avoid memory spikes
        List<Map.Entry<String, String>> entries = new ArrayList<>(bookMap.entrySet());

        for (int i = 0; i < entries.size(); i += batchSize) {
            int end = Math.min(i + batchSize, entries.size());
            List<Map.Entry<String, String>> batch = entries.subList(i, end);

            for (Map.Entry<String, String> entry : batch) {
                hazelcastMap.put(entry.getKey(), Collections.singletonList(entry.getValue()));
            }
        }

        System.out.println("Datalake charged.");
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public void loadData() {
        Map<String, String> hazelcastMap = hazelcastInstance.getMap("datamart-map");
        IMap<String, Boolean> processedFolders = hazelcastInstance.getMap("processed-folders");  // Map of processed subfolders
        IAtomicLong currentFolderIndex = hazelcastInstance.getCPSubsystem().getAtomicLong("currentFolderIndex");  // Atomic counter for progress tracking
        File baseDirectory = new File(dataMartPath);
        File[] folders = baseDirectory.listFiles(File::isDirectory);

        if (folders == null) {
            System.err.println("Path not found: " + dataMartPath);
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(1);  // One thread to process one folder at a time
        List<Callable<Void>> tasks = new ArrayList<>();

        // Iterate over the folders and assign processing sequentially
        for (File folder : folders) {
            tasks.add(() -> {
                // Get the index of the folder to be processed by the server
                long folderIndex = currentFolderIndex.incrementAndGet();  // Get the next folder index

                // Assign the folder to a server based on the index
                if (folderIndex <= folders.length) {
                    // Check if the folder has already been processed
                    if (processedFolders.putIfAbsent(folder.getName(), true) == null) {  // Only process if not marked as processed
                        System.out.println("Processing folder: " + folder.getName());

                        WordList wordList = new WordList(folder.getAbsolutePath());
                        List<Map<String, String>> wordsList = wordList.wordMapCreator();

                        int batchSize = 100;  // Reduce batch size to 100 words
                        Map<String, String> batchMap = new HashMap<>();

                        for (Map<String, String> wordData : wordsList) {
                            for (Map.Entry<String, String> entry : wordData.entrySet()) {

                                batchMap.put(entry.getKey(), entry.getValue());

                                if (batchMap.size() >= batchSize) {
                                    hazelcastMap.putAll(new HashMap<>(batchMap));
                                    batchMap.clear();  // Immediately free memory
                                }
                            }
                        }

                        // Save any remaining words
                        if (!batchMap.isEmpty()) {
                            hazelcastMap.putAll(batchMap);
                            batchMap.clear();
                        }

                        System.out.println("Finished processing: " + folder.getName());
                    }
                }

                return null;
            });
        }

        try {
            executorService.invokeAll(tasks);
            executorService.shutdown();
            executorService.awaitTermination(20, TimeUnit.MINUTES);  // Ensure all threads finish correctly
            System.out.println("Datamart fully loaded");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Error during concurrent loading: " + e.getMessage());
        }
    }
}