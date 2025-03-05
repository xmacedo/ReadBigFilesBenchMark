package br.com.xmacedo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static br.com.xmacedo.Constants.PATH_FILE;

public class ConcurrencyWithExecutorService {

    public static void main(String[] args) {
        processFileConcurrent(PATH_FILE);
    }

    private static void processFileConcurrent(String filename) {
        long startTime = System.currentTimeMillis();

        // Create a thread pool with a number of threads = CPU cores
        int numThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Number of Threads used: " + numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // A list to hold Future results from each submitted task
        final int BATCH_SIZE = 1_000_000;
        List<String> linesBatch = new ArrayList<>(BATCH_SIZE);

        // Read file and create tasks
        Map<String, StatsModel> finalStatsMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                linesBatch.add(line);
                if (linesBatch.size() >= BATCH_SIZE) {
                    Future<Map<String, StatsModel>> future = submitBatch(executor, linesBatch);
                    try {
                        mergePartialResults(finalStatsMap, future.get()); // Já processa o resultado
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    linesBatch.clear(); // Libera memória
                }
            }

            // Submit the leftover lines if there are any
            if (!linesBatch.isEmpty()) {
                Future<Map<String, StatsModel>> future = submitBatch(executor, linesBatch);
                try {
                    mergePartialResults(finalStatsMap, future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.out.println("Error on Read file.");
            e.printStackTrace();
        }

        // Shutdown executor
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Print results
        System.out.print("{");
        for (Map.Entry<String, StatsModel> entry : finalStatsMap.entrySet()) {
            StatsModel st = entry.getValue();
            double avg = st.getSum() / st.getCount();
            System.out.printf("%s=%.1f/%.1f/%.1f, ", entry.getKey(), st.getMin(), avg, st.getMax());
        }
        System.out.println("\b\b}");
        System.out.println("Concurrent duration: " + (duration / 1000.0) + " seconds");
    }

    private static Future<Map<String, StatsModel>> submitBatch(ExecutorService executor, List<String> batch) {
        return executor.submit(() -> {
            Map<String, StatsModel> localMap = new HashMap<>();
            for (String line : batch) {
                String[] parts = line.split(";");
                if (parts.length < 2) {
                    continue;
                }
                String propertyId = parts[0];
                double price;
                try {
                    price = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    continue;
                }

                StatsModel st = localMap.get(propertyId);
                if (st == null) {
                    st = StatsModel.builder()
                            .count(1)
                            .min(price)
                            .max(price)
                            .sum(price)
                            .build();
                    localMap.put(propertyId, st);
                } else {
                    if (price < st.getMin()) st.setMin(price);
                    if (price > st.getMax()) st.setMax(price);
                    st.setSum(st.getSum() + price);
                    st.setCount(st.getCount() + 1);
                }
            }
            return localMap;
        });
    }

    private static void mergePartialResults(Map<String, StatsModel> finalMap, Map<String, StatsModel> partial) {
        for (Map.Entry<String, StatsModel> entry : partial.entrySet()) {
            finalMap.merge(entry.getKey(), entry.getValue(), (existing, incoming) -> {
                if (incoming.getMin() < existing.getMin()) existing.setMin(incoming.getMin());
                if (incoming.getMax() > existing.getMax()) existing.setMax(incoming.getMax());
                existing.setSum(existing.getSum() + incoming.getSum());
                existing.setCount(existing.getCount() + incoming.getCount());
                return existing;
            });
        }
    }
}