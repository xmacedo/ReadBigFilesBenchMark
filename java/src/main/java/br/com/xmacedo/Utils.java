package br.com.xmacedo;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static void printResults(String typeProcess, long duration, Map<String, StatsModel> statsMap) {
        System.out.print("{");
        statsMap.forEach((prop, st) -> {
            double avg = st.getSum() / st.getCount();
            System.out.printf("%s=%.1f/%.1f/%.1f, ", prop, st.getMin(), avg, st.getMax());
        });
        System.out.println("\b\b}");

        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60;
        long milliseconds = duration % 1000;

        System.out.print("'" + typeProcess + "' took the duration of: " + String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds));
    }
}
