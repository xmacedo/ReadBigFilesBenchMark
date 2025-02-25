package br.com.xmacedo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SingleThreadProcessor {

    public static void main(String[] args) throws IOException {
        String filename = "data/real_estate_prices.txt"; // Large file (1B rows)
        processFileSingleThread(filename);
    }

    private static void processFileSingleThread(String filename) throws IOException {
        Map<String, StatusModel> statsMap = new HashMap<>();
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

                StatusModel current = statsMap.get(propertyId);
                if (current == null) {
                    current = new StatusModel();
                    current.setMin(price);
                    current.setMax(price);
                    current.setSum(price);
                    current.setCount(1);
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


            // Print results (could be huge, so be mindful in real scenarios)
            System.out.print("{");
            for (Map.Entry<String, StatusModel> entry : statsMap.entrySet()) {
                String prop = entry.getKey();
                StatusModel st = entry.getValue();
                double avg = st.getSum() / st.getCount();
                System.out.printf("%s=%.1f/%.1f/%.1f, ", prop, st.getMin(), avg, st.getMax());
            }
            System.out.println("\b\b}");

            System.out.println("Single-threaded duration: " + (duration / 1000.0) + " seconds");
        } else {
            System.out.println("Single-threaded problem to Read/processing file!");
        }
    }
}