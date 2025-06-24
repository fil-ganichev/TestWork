package ru.ganichev.task1;

public interface ClassBInterface extends PostInit {

    String CALCULATOR_VERSION = "calc v1.0";

    void calculateInterest(double amount, double[] interest);

    void calculateInterest(double amount, double[] interest, String tariff);

    void calculateInterest(double amount, double[] interest, String tariff, Double rate);

    void calculateInterest(double amount, double[] interest, Double rate);

    void calculateInterest(double amount, double[] interest, Double rate, Double commAmt);

    void calculateInterest(double amount, double[] interest, String tariff, Double rate, Double commAmt);

    default String getCalculatorVersion() {
        return CALCULATOR_VERSION;
    }
}
