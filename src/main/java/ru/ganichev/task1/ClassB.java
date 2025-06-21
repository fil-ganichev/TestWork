package ru.ganichev.task1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.ganichev.task1.exception.DefaultRateNotInitializedException;

import javax.annotation.PostConstruct;

import static ru.ganichev.task1.DefaultRateHolder.NOT_DEFINED;
import static ru.ganichev.task1.DefaultRateHolder.NOT_DEFINED_STR;

@Component
public class ClassB implements ClassBInterface {

    private static final String CALCULATOR_VERSION = "calc v1.0";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final DefaultRateHolder defaultRateHolder;

    public ClassB(DefaultRateHolder defaultRateHolder) {
        this.defaultRateHolder = defaultRateHolder;
    }

    @PostConstruct
    void init() {
        log.info("init package ClassB");
        double defaultRate = defaultRateHolder.getDefaultRate();
        if (defaultRate != DefaultRateHolder.NOT_DEFINED) {
            log.info("using default rate = {}", defaultRate);
        } else {
            log.info("using default rate = {}", NOT_DEFINED_STR);
        }
    }

    @Override
    public void calculateInterest(double amount, double[] interest) {
        calculateInterest(amount, interest, "DEF", defaultRateHolder.getDefaultRate(), null);
    }

    @Override
    public void calculateInterest(double amount, double[] interest, String tariff) {
        calculateInterest(amount, interest, tariff, defaultRateHolder.getDefaultRate(), null);
    }

    @Override
    public void calculateInterest(double amount, double[] interest, String tariff, Double rate) {
        calculateInterest(amount, interest, tariff, rate, null);
    }

    @Override
    public void calculateInterest(double amount, double[] interest, Double rate) {
        calculateInterest(amount, interest, "DEF", rate, null);
    }

    @Override
    public void calculateInterest(double amount, double[] interest, Double rate, Double commAmt) {
        calculateInterest(amount, interest, "DEF", rate, commAmt);
    }

    @Override
    public void calculateInterest(double amount, double[] interest, String tariff, Double rate, Double commAmt) {
        double calculatedRate = (rate != null) ? rate : defaultRateHolder.getDefaultRate();
        if(calculatedRate==NOT_DEFINED)  {
            throw new DefaultRateNotInitializedException();
        }
        double commission = (commAmt != null) ? commAmt : 0.0;
        interest[0] = amount * calculatedRate - commission;
        log.info("do calc interest for amount=" + amount +
                "; tariff=" + tariff +
                "; rate=" + calculatedRate +
                "; p_comm_amt=" + (commAmt == null ? "" : commAmt));
    }

    @Override
    public String getCalculatorVersion() {
        return CALCULATOR_VERSION;
    }
}
