package ru.ganichev.task1;

public interface ClassBInterface {

    void calculateInterest(double amount, double[] interest);
    void calculateInterest(double amount, double[] interest, String tariff);

    void calculateInterest(double amount, double[] interest, String tariff, Double rate);
    void calculateInterest(double amount, double[] interest, Double rate);
    void calculateInterest(double amount, double[] interest, Double rate, Double commAmt);
    void calculateInterest(double amount, double[] interest, String tariff, Double rate, Double commAmt);
    String getCalculatorVersion();
}
