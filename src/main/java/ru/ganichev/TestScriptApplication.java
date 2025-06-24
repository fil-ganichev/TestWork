package ru.ganichev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ganichev.task1.ClassAInterface;
import ru.ganichev.task1.ClassBInterface;
import ru.ganichev.task1.SessionHolder;

import java.util.concurrent.*;

public class TestScriptApplication {

    private static final Logger log = LoggerFactory.getLogger("Main");

    private static ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {
        SessionHolder sessionHolder = new SessionHolder();
        CountDownLatch countDownLatch = new CountDownLatch(2);

        executorService.submit(() -> {
            try {
                countDownLatch.countDown();
                countDownLatch.await();
                runTest(sessionHolder);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
        executorService.submit(() -> {
            try {
                countDownLatch.countDown();
                countDownLatch.await();
                runTest(sessionHolder);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });

        executorService.shutdown();
        executorService.awaitTermination(3000, TimeUnit.MILLISECONDS);
    }

    private static void runTest(SessionHolder sessionHolder) {
        log.info("*** TEST1");
        double[] interest = new double[1];

        ClassAInterface classAInterface = sessionHolder.getClassAInterface();
        ClassBInterface classBInterface = sessionHolder.getClassBInterface();

        classBInterface.calculateInterest(2000, interest, 0.1d, 100d);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST2");
        classAInterface.setDefaultRate(0.5);
        interest[0] = sessionHolder.getClassAInterface().calculateDefaultInterest(1000);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST3");
        classAInterface.setDefaultRate(0.6);
        interest[0] = sessionHolder.getClassAInterface().calculateSpecialInterest(1000);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST4");
        interest[0] = classAInterface.calculateSpecialInterest(1000, 0.3);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST5");
        interest[0] = classAInterface.calculateSpecialInterest(1000, null);
        log.info("interest = {}", interest[0]);
    }
}
