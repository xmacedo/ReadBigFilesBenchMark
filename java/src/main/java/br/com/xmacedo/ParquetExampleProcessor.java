package br.com.xmacedo;

import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetReader.Builder;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ParquetExampleProcessor {

    public static void main(String[] args) throws IOException {
        String parquetFile = "data/real_estate_prices.parquet";
        processParquet(parquetFile);
    }

    private static void processParquet(String parquetFile) throws IOException {
        long startTime = System.currentTimeMillis();

        Configuration conf = new Configuration();
        HadoopInputFile inputFile = HadoopInputFile.fromPath(
                new org.apache.hadoop.fs.Path(Paths.get(parquetFile).toAbsolutePath().toString()),
                conf
        );
        Builder<GenericData.Record> builder = AvroParquetReader.builder(inputFile);
        Map<String, StatsModel> statsMap = new HashMap<>();
        try (ParquetReader<GenericData.Record> reader = builder.build()) {
            GenericData.Record record;
            while ((record = reader.read()) != null) {
                // Assuming the schema has fields: property_id (string), price (double)
                String propertyId = record.get("property_id").toString();
                double price = (double) record.get("price");

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
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Utils.printResults("Parquet processing ", duration, statsMap);
    }
}
