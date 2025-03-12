package br.com.xmacedo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static br.com.xmacedo.Constants.PATH_FILE;

public class SingleThreadProcessor {

    public static void main(String[] args) {
        processFileSingleThread(PATH_FILE);
    }

    private static void processFileSingleThread(String filename) {
        Map<String, StatsModel> statsMap = new HashMap<>();
        boolean readOk = true;
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

                StatsModel current = statsMap.get(propertyId);
                if (current == null) {
                    current = StatsModel.builder()
                            .count(1)
                            .min(price)
                            .max(price)
                            .sum(price)
                            .build();
                    statsMap.put(propertyId, current);
                } else {
                    if (price < current.getMin()) current.setMin(price);
                    if (price > current.getMax()) current.setMax(price);
                    current.setSum(price + current.getSum());
                    current.setCount(current.getCount() + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            readOk = false;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        if (readOk) {
            Utils.printResults("Single-threaded", duration, statsMap);
        } else {
            System.out.println("Single-threaded problem to Read/processing file!");
        }
    }
}