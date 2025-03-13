package br.com.xmacedo;

import reactor.core.publisher.Flux;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import static br.com.xmacedo.Constants.PATH_FILE;

public class ParallelStreamWithFluxProcessor {

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        ConcurrentMap<String, StatsModel> statsMap = new ConcurrentHashMap<>();

        try (Stream<String> lines = Files.lines(Paths.get(PATH_FILE))) {
            Flux.fromStream(lines)
                    .parallel()
                    .map(line -> line.split(";"))
                    .filter(parts -> parts.length >= 2)
                    .doOnNext(parts -> {
                        String key = parts[0];
                        double value = parsePrice(parts[1]);

                        if (!Double.isNaN(value)) {
                            statsMap.compute(key, (k, st) -> (st == null) ?
                                    StatsModel.builder().min(value).max(value).sum(value).count(1).build() :
                                    StatsModel.combine(st, StatsModel.builder().min(value).max(value).sum(value).count(1).build()));
                        }
                    })
                    .sequential()
                    .blockLast();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        // Print results
        Utils.printResults("Parallel Stream With Flux", duration, statsMap);
    }

    private static double parsePrice(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
