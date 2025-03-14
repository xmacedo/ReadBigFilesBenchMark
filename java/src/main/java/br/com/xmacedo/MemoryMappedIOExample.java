package br.com.xmacedo;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static br.com.xmacedo.Constants.PATH_FILE;

public class MemoryMappedIOExample {
    private static ConcurrentMap<String, StatsModel> statsMap = new ConcurrentHashMap<>();
    private static final long CHUNK_SIZE = 1L * 1024 * 1024 * 1024; // 1GB per read

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();

        Path path = Path.of(PATH_FILE);
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = fileChannel.size();
            long position = 0;
            StringBuilder line = new StringBuilder();

            while (position < fileSize) {
                long remaining = fileSize - position;
                long chunkSize = Math.min(CHUNK_SIZE, remaining);

                // Mapeia um bloco de até 1GB
                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, position, chunkSize);

                for (int i = 0; i < chunkSize; i++) {
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

                position += chunkSize;

                if (line.length() > 0 && position < fileSize) {
                    position = adjustPosition(fileChannel, position);
                }
            }

            if (line.length() > 0) {
                processLine(line.toString());
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Print results
        Utils.printResults("Memory Mapped IO", duration, statsMap);
    }

    private static long adjustPosition(FileChannel fileChannel, long position) throws IOException {
        while (position < fileChannel.size()) {
            fileChannel.position(position);
            byte[] oneByte = new byte[1];
            fileChannel.read(java.nio.ByteBuffer.wrap(oneByte));

            if ((char) oneByte[0] == '\n') {
                position++;
                break;
            }

            position++;
        }

        return position;
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
