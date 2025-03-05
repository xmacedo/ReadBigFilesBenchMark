package br.com.xmacedo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static br.com.xmacedo.Constants.PATH_FILE;

public class ParallelStreamProcessor {

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        // Read all lines (caution: memory usage for 1B lines might be enormous)
        List<String> lines = Files.readAllLines(Paths.get(PATH_FILE));

        // Use a concurrent map: propertyID -> Stats
        Map<String, StatsModel> statsMap = lines
                .parallelStream()
                .map(line -> line.split(";"))
                .filter(parts -> parts.length >= 2)
                .map(parts -> new AbstractMap.SimpleEntry<>(parts[0], parsePrice(parts[1])))
                .filter(entry -> !Double.isNaN(entry.getValue())) // skip invalid
                .collect(Collectors.toConcurrentMap(
                        AbstractMap.SimpleEntry::getKey,
                        e -> StatsModel.builder()
                                .min(e.getValue())
                                .max(e.getValue())
                                .sum(e.getValue())
                                .count(1)
                                .build(),
                        (st1, st2) -> StatsModel.combine(st1, st2)
                ));

        long endTime = System.currentTimeMillis();

        // Print results
        System.out.print("{");
        statsMap.forEach((prop, st) -> {
            double avg = st.getSum() / st.getCount();
            System.out.printf("%s=%.1f/%.1f/%.1f, ", prop, st.getMin(), avg, st.getMax());
        });
        System.out.println("\b\b}");
        System.out.println("Parallel stream duration: " + ((endTime - startTime) / 1000.0) + " seconds");
    }

    private static double parsePrice(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
