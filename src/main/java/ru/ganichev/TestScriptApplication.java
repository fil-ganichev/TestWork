package ru.ganichev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.ganichev.task1.ClassAInterface;
import ru.ganichev.task1.ClassBInterface;

@SpringBootApplication
public class TestScriptApplication {

    private static final Logger log = LoggerFactory.getLogger("Main");

    public static void main(String[] args) {
        log.info("*** TEST1");
        ConfigurableApplicationContext context = SpringApplication.run(TestScriptApplication.class, args);
        double[] interest = new double[1];
        ClassAInterface classA = context.getBean(ClassAInterface.class);
        ClassBInterface classB = context.getBean(ClassBInterface.class);

        classB.calculateInterest(2000, interest, 0.1d, 100d);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST2");
        classA.setDefaultRate(0.5);
        interest[0] = classA.calculateDefaultInterest(1000);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST3");
        classA.setDefaultRate(0.6);
        interest[0]=classA.calculateSpecialInterest(1000);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST4");
        interest[0]=classA.calculateSpecialInterest(1000, 0.3);
        log.info("interest = {}", interest[0]);

        log.info("");

        log.info("*** TEST5");
        interest[0]=classA.calculateSpecialInterest(1000, null);
        log.info("interest = {}", interest[0]);

    }
}
