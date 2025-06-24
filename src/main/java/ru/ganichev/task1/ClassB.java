package ru.ganichev.task1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ganichev.task1.exception.DefaultRateNotInitializedException;

import static ru.ganichev.task1.Constants.NOT_DEFINED_RATE;
import static ru.ganichev.task1.Constants.NOT_DEFINED_RATE_STR;

public class ClassB implements ClassBInterface {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final SessionHolder sessionHolder;

    public ClassB(SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }

    @Override
    public void postInit() {
        log.info("init package ClassB");
        double defaultRate = sessionHolder.getClassAInterface().getDefaultRate();
        if (defaultRate != NOT_DEFINED_RATE) {
            log.info("using default rate = {}", defaultRate);
        } else {
            log.info("using default rate = {}", NOT_DEFINED_RATE_STR);
        }
    }

    @Override
    public void calculateInterest(double amount, double[] interest) {
        calculateInterest(amount, interest, "DEF", sessionHolder.getClassAInterface().getDefaultRate(), null);
    }

    @Override
    public void calculateInterest(double amount, double[] interest, String tariff) {
        calculateInterest(amount, interest, tariff, sessionHolder.getClassAInterface().getDefaultRate(), null);
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
        double calculatedRate = (rate != null) ? rate : sessionHolder.getClassAInterface().getDefaultRate();
        if (calculatedRate == NOT_DEFINED_RATE) {
            throw new DefaultRateNotInitializedException();
        }
        double commission = (commAmt != null) ? commAmt : 0.0;
        interest[0] = amount * calculatedRate - commission;
        log.info("do calc interest for amount=" + amount +
                "; tariff=" + tariff +
                "; rate=" + calculatedRate +
                "; p_comm_amt=" + (commAmt == null ? "" : commAmt));
    }
}
