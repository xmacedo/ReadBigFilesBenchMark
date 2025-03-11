package br.com.xmacedo;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static br.com.xmacedo.Constants.PATH_FILE;

public class MemoryMappedIOExample {
    private static ConcurrentMap<String, StatsModel> statsMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();

        Path path = Path.of(PATH_FILE);
        ConcurrentMap<String, StatsModel> statsMap = new ConcurrentHashMap<>();
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

            StringBuilder line = new StringBuilder();

            for (int i = 0; i < fileSize; i++) {
                char c = (char) buffer.get();

                if (c == '\n' || c == '\r') {
                    if (line.length() > 0) {
                        processLine(line.toString());
                        line.setLength(0);
                    }
                } else {
                    line.append(c);
                }
            }

            if (line.length() > 0) {
                processLine(line.toString());
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Print results
        System.out.print("{");
        for (Map.Entry<String, StatsModel> entry : statsMap.entrySet()) {
            StatsModel st = entry.getValue();
            double avg = st.getSum() / st.getCount();
            System.out.printf("%s=%.1f/%.1f/%.1f, ", entry.getKey(), st.getMin(), avg, st.getMax());
        }
        System.out.println("\b\b}");
        System.out.println("Concurrent duration: " + (duration / 1000.0) + " seconds");
    }

    private static void processLine(String line) {
        // Split line and start to process then
        String[] parts = line.split(";");
        if (parts.length >= 2) {
            String key = parts[0];
            double value = parsePrice(parts[1]);

            if (!Double.isNaN(value)) {
                statsMap.compute(key, (k, st) -> (st == null) ?
                        StatsModel.builder().min(value).max(value).sum(value).count(1).build() :
                        StatsModel.combine(st, StatsModel.builder().min(value).max(value).sum(value).count(1).build()));
            }
        }
    }

    private static double parsePrice(String price) {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
