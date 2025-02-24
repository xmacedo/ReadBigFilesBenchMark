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

public class ConcurrencyWithExecutorService {
    static class Stats {
        double min;
        double max;
        double sum;
        long count;
    }

    public static void main(String[] args) {
        String filename = "data/real_estate_prices.txt";
        processFileConcurrent(filename);
    }

    private static void processFileConcurrent(String filename) {
        long startTime = System.currentTimeMillis();

        // Create a thread pool with a number of threads = CPU cores
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // A list to hold Future results from each submitted task
        List<Future<Map<String, Stats>>> futures = new ArrayList<>();

        final int BATCH_SIZE = 500_000;
        List<String> linesBatch = new ArrayList<>(BATCH_SIZE);

        // Read file and create tasks
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                linesBatch.add(line);
                if (linesBatch.size() >= BATCH_SIZE) {
                    // Submit a new task
                    futures.add(submitBatch(executor, linesBatch));
                    linesBatch = new ArrayList<>(BATCH_SIZE);
                }
            }
            // Submit the leftover lines if there are any
            if (!linesBatch.isEmpty()) {
                futures.add(submitBatch(executor, linesBatch));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Merge all partial results
        Map<String, Stats> finalStatsMap = new HashMap<>();
        for (Future<Map<String, Stats>> future : futures) {
            try {
                Map<String, Stats> partial = future.get(); // blocking call
                mergePartialResults(finalStatsMap, partial);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        // Shutdown executor
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Print results
        System.out.print("{");
        for (Map.Entry<String, Stats> entry : finalStatsMap.entrySet()) {
            Stats st = entry.getValue();
            double avg = st.sum / st.count;
            System.out.printf("%s=%.1f/%.1f/%.1f, ", entry.getKey(), st.min, avg, st.max);
        }
        System.out.println("\b\b}");
        System.out.println("Concurrent duration: " + (duration / 1000.0) + " seconds");
    }

    private static Future<Map<String, Stats>> submitBatch(ExecutorService executor, List<String> batch) {
        return executor.submit(() -> {
            Map<String, Stats> localMap = new HashMap<>();
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

                Stats st = localMap.get(propertyId);
                if (st == null) {
                    st = new Stats();
                    st.min = price;
                    st.max = price;
                    st.sum = price;
                    st.count = 1;
                    localMap.put(propertyId, st);
                } else {
                    if (price < st.min) st.min = price;
                    if (price > st.max) st.max = price;
                    st.sum += price;
                    st.count++;
                }
            }
            return localMap;
        });
    }

    private static void mergePartialResults(Map<String, Stats> finalMap, Map<String, Stats> partial) {
        for (Map.Entry<String, Stats> entry : partial.entrySet()) {
            finalMap.merge(entry.getKey(), entry.getValue(), (existing, incoming) -> {
                if (incoming.min < existing.min) existing.min = incoming.min;
                if (incoming.max > existing.max) existing.max = incoming.max;
                existing.sum += incoming.sum;
                existing.count += incoming.count;
                return existing;
            });
        }
    }
}