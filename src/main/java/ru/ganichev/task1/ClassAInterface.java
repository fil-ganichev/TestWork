package ru.ganichev.task1;

public interface ClassAInterface extends PostInit {

    void setDefaultRate(double rate);

    double getDefaultRate();

    double calculateDefaultInterest(double amount);

    double calculateSpecialInterest(double amount);

    double calculateSpecialInterest(double amount, Double rate);
}
