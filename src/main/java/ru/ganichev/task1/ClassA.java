package ru.ganichev.task1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static ru.ganichev.task1.DefaultRateHolder.NOT_DEFINED;

@Component
public class ClassA implements ClassAInterface {

    private static double SPECIAL_RATE = 0.2;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final DefaultRateHolder defaultRateHolder;
    private final ClassBInterface classB;

    public ClassA(DefaultRateHolder defaultRateHolder, ClassBInterface classB) {
        this.defaultRateHolder = defaultRateHolder;
        this.classB = classB;
    }

    @PostConstruct
    void init() {
        log.info("init package ClassA");
        log.info("using calculator: {}", classB.getCalculatorVersion());
        initDefaultRate();
    }

    @Override
    public void setDefaultRate(double rate) {
        log.info("change default rate from {} to {}", getDefaultRateStr(), rate);
        defaultRateHolder.setDefaultRate(rate);
    }

    @Override
    public double getDefaultRate() {
        return defaultRateHolder.getDefaultRate();
    }

    @Override
    public double calculateDefaultInterest(double amount) {
        double[] resultAmount = new double[]{1000.0};
        classB.calculateInterest(amount, resultAmount, "TARIFF1", null, 10.0);
        return resultAmount[0];
    }

    @Override
    public double calculateSpecialInterest(double amount) {
        return calculateSpecialInterest(amount, getDefaultRate());
    }

    @Override
    public double calculateSpecialInterest(double amount, Double rate) {
        double actualRate = (rate != null && rate != NOT_DEFINED) ? rate : getSpecialRate();
        double[] resultAmount = new double[]{1000.0};
        classB.calculateInterest(amount, resultAmount, actualRate);
        return resultAmount[0];
    }

    private void initDefaultRate() {
        double value = 0.1;
        defaultRateHolder.setDefaultRate(value);
        log.info("init default rate to value: {}", value);
    }

    private double getSpecialRate() {
        log.info("special rate requested");
        return SPECIAL_RATE;
    }

    private String getDefaultRateStr() {
        double defaultRate = defaultRateHolder.getDefaultRate();
        if (defaultRate == NOT_DEFINED) {
            return DefaultRateHolder.NOT_DEFINED_STR;
        }
        return String.valueOf(defaultRate);
    }
}
