package br.com.xmacedo;


import lombok.Builder;

@Builder
public class StatsModel {
    private double min;
    private double max;
    private double sum;
    private long count;

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public static StatsModel combine(StatsModel a, StatsModel b) {
        return StatsModel.builder()
                .min(Math.min(a.min, b.min))
                .max(Math.max(a.max, b.max))
                .sum(a.getSum() + b.getSum())
                .build();
    }
}
