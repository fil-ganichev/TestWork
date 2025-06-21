package ru.ganichev.task1;

import org.springframework.stereotype.Component;

@Component
public class DefaultRateHolder {

    public static final double NOT_DEFINED = Double.MIN_VALUE;
    public static final String NOT_DEFINED_STR = "not defined";
    private final ThreadLocal<DoubleHolder> defaultRate = new ThreadLocal<>();

    public double getDefaultRate() {
        DoubleHolder doubleHolder = defaultRate.get();
        if (doubleHolder == null) {
            return NOT_DEFINED;
        }
        return doubleHolder.value;
    }

    public void setDefaultRate(double value) {
        DoubleHolder doubleHolder = defaultRate.get();
        doubleHolder = doubleHolder == null ? new DoubleHolder() : doubleHolder;
        doubleHolder.value = value;
        defaultRate.set(doubleHolder);
    }

    private static class DoubleHolder {
        private double value;
    }
}
