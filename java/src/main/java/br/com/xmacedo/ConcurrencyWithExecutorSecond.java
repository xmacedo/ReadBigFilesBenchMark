package br.com.xmacedo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import static br.com.xmacedo.Constants.PATH_FILE;

public class ConcurrencyWithExecutorSecond {
    private static final int BATCH_SIZE = 100_000;
    private static final int MAX_CONCURRENT_TASKS = 10;
    private static final ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) {
        processFileConcurrent(PATH_FILE);
    }

    public static void processFileConcurrent(String filename) {
        long startTime = System.currentTimeMillis();
        System.out.println("Number of Threads used: " + pool.getParallelism());

        ConcurrentHashMap<String, StatsModel> finalStatsMap = new ConcurrentHashMap<>();
        List<CompletableFuture<Map<String, StatsModel>>> futures = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            List<String> batch = new ArrayList<>(BATCH_SIZE);

            String line;
            while ((line = reader.readLine()) != null) {
                batch.add(line);
                if (batch.size() >= BATCH_SIZE) {
                    futures.add(processBatchAsync(new ArrayList<>(batch)));
                    batch.clear();
                }

                if (futures.size() >= MAX_CONCURRENT_TASKS) {
                    mergeAndClearFutures(futures, finalStatsMap);
                }
            }
            if (!batch.isEmpty()) {
                futures.add(processBatchAsync(new ArrayList<>(batch)));
            }
            // Wait all process finished
            mergeAndClearFutures(futures, finalStatsMap);

        } catch (IOException e) {
            System.out.println("Error on Read file.");
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Utils.printResults("Concurrency With Executor (Different way)", duration, finalStatsMap);
    }

    private static CompletableFuture<Map<String, StatsModel>> processBatchAsync(List<String> batch) {
        return CompletableFuture.supplyAsync(() -> processBatch(batch), pool);
    }

    private static Map<String, StatsModel> processBatch(List<String> batch) {
        return batch.parallelStream()
                .map(ConcurrencyWithExecutorSecond::parseLine)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Pair::getKey,
                        p -> new StatsModel(p.getValue(), p.getValue(), p.getValue(), 1),
                        (existing, incoming) -> {
                            existing.setMin(Math.min(existing.getMin(), incoming.getMin()));
                            existing.setMax(Math.max(existing.getMax(), incoming.getMax()));
                            existing.setSum(existing.getSum() + incoming.getSum());
                            existing.setCount(existing.getCount() + incoming.getCount());
                            return existing;
                        }
                ));
    }

    private static Pair<String, Double> parseLine(String line) {
        String[] parts = line.split(";");
        if (parts.length < 2) return null;
        try {
            return new Pair<>(parts[0], Double.parseDouble(parts[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void mergeAndClearFutures(List<CompletableFuture<Map<String, StatsModel>>> futures, ConcurrentHashMap<String, StatsModel> finalStatsMap) {
        futures.forEach(f -> f.thenAccept(partialMap -> mergePartialResults(finalStatsMap, partialMap)).join());
        futures.clear();
        System.gc();
    }

    private static void mergePartialResults(ConcurrentHashMap<String, StatsModel> finalMap, Map<String, StatsModel> partial) {
        partial.forEach((key, incoming) ->
                finalMap.merge(key, incoming, (existing, newStats) -> {
                    existing.setMin(Math.min(existing.getMin(), newStats.getMin()));
                    existing.setMax(Math.max(existing.getMax(), newStats.getMax()));
                    existing.setSum(existing.getSum() + newStats.getSum());
                    existing.setCount(existing.getCount() + newStats.getCount());
                    return existing;
                })
        );
    }


    static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}