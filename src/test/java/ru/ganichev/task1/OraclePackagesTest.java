package ru.ganichev.task1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestOraclePackagesConfiguration.class)
public class OraclePackagesTest {

    private static final int MAX_WAIT_MS = 3000;
    @Autowired
    private ClassAInterface classA;
    @Autowired
    private ClassBInterface classB;
    @Autowired
    private ExecutorService testExecutor;

    @Test
    void when_packagesRunInMultiThread_then_resultsConsistent() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(4);
        CountDownLatch finishLatch = new CountDownLatch(4);
        double[] testResults = new double[4];
        testExecutor.submit(() -> {
            startLatch.countDown();
            double[] interest = new double[1];
            try {
                startLatch.await();
                classB.calculateInterest(2000, interest, 0.1d, 100d);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                testResults[0] = interest[0];
                finishLatch.countDown();
            }
        });
        testExecutor.submit(() -> {
            startLatch.countDown();
            double[] interest = new double[1];
            try {
                startLatch.await();
                classA.setDefaultRate(0.5);
                interest[0] = classA.calculateDefaultInterest(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                testResults[1] = interest[0];
                finishLatch.countDown();
            }
        });
        testExecutor.submit(() -> {
            startLatch.countDown();
            double[] interest = new double[1];
            try {
                startLatch.await();
                classA.setDefaultRate(0.6);
                interest[0] = classA.calculateSpecialInterest(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                testResults[2] = interest[0];
                finishLatch.countDown();
            }
        });
        testExecutor.submit(() -> {
            startLatch.countDown();
            double[] interest = new double[1];
            try {
                startLatch.await();
                interest[0] = classA.calculateSpecialInterest(1000, 0.3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                testResults[3] = interest[0];
                finishLatch.countDown();
            }
        });
        finishLatch.await(MAX_WAIT_MS, TimeUnit.MILLISECONDS);
        double[] expectedResults = new double[]{100d, 490d, 600d, 300d};
        assertThat(testResults).isEqualTo(expectedResults);
    }
}
