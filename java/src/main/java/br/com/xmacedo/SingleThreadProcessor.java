package br.com.xmacedo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SingleThreadProcessor {

    static class Stats {
        double min;
        double max;
        double sum;
        long count;
    }

    public static void main(String[] args) throws IOException {
        String filename = "data/real_estate_prices.txt"; // Large file (1B rows)
        processFileSingleThread(filename);
    }

    private static void processFileSingleThread(String filename) throws IOException {
        Map<String, Stats> statsMap = new HashMap<>();

        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
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

                Stats current = statsMap.get(propertyId);
                if (current == null) {
                    current = new Stats();
                    current.min = price;
                    current.max = price;
                    current.sum = price;
                    current.count = 1;
                    statsMap.put(propertyId, current);
                } else {
                    if (price < current.min) current.min = price;
                    if (price > current.max) current.max = price;
                    current.sum += price;
                    current.count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Print results (could be huge, so be mindful in real scenarios)
        System.out.print("{");
        for (Map.Entry<String, Stats> entry : statsMap.entrySet()) {
            String prop = entry.getKey();
            Stats st = entry.getValue();
            double avg = st.sum / st.count;
            System.out.printf("%s=%.1f/%.1f/%.1f, ", prop, st.min, avg, st.max);
        }
        System.out.println("\b\b}");

        System.out.println("Single-threaded duration: " + (duration / 1000.0) + " seconds");
    }
}