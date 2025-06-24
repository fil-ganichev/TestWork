package ru.ganichev.task1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ru.ganichev.task1.Constants.NOT_DEFINED_RATE;
import static ru.ganichev.task1.Constants.NOT_DEFINED_RATE_STR;

public class ClassA implements ClassAInterface {

    private static double SPECIAL_RATE = 0.2;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SessionHolder sessionHolder;

    public ClassA(SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    @Override
    public void postInit() {
        log.info("init package ClassA");
        log.info("using calculator: {}", sessionHolder.getClassBInterface().getCalculatorVersion());
        initDefaultRate();
    }

    @Override
    public void setDefaultRate(double rate) {
        log.info("change default rate from {} to {}", getDefaultRateStr(), rate);
        sessionHolder.setDefaultRate(rate);
    }

    @Override
    public double getDefaultRate() {
        return sessionHolder.getDefaultRate();
    }

    @Override
    public double calculateDefaultInterest(double amount) {
        double[] resultAmount = new double[]{1000.0};
        sessionHolder.getClassBInterface().calculateInterest(amount, resultAmount, "TARIFF1", null, 10.0);
        return resultAmount[0];
    }

    @Override
    public double calculateSpecialInterest(double amount) {
        return calculateSpecialInterest(amount, getDefaultRate());
    }

    @Override
    public double calculateSpecialInterest(double amount, Double rate) {
        double actualRate = (rate != null && rate != NOT_DEFINED_RATE) ? rate : getSpecialRate();
        double[] resultAmount = new double[]{1000.0};
        sessionHolder.getClassBInterface().calculateInterest(amount, resultAmount, actualRate);
        return resultAmount[0];
    }

    private void initDefaultRate() {
        double value = 0.1;
        sessionHolder.setDefaultRate(value);
        log.info("init default rate to value: {}", value);
    }

    private double getSpecialRate() {
        log.info("special rate requested");
        return SPECIAL_RATE;
    }

    private String getDefaultRateStr() {
        if (sessionHolder.getDefaultRate() == NOT_DEFINED_RATE) {
            return NOT_DEFINED_RATE_STR;
        }
        return String.valueOf(sessionHolder.getDefaultRate());
    }
}
