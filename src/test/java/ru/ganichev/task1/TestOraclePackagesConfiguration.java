package ru.ganichev.task1;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TestConfiguration
public class TestOraclePackagesConfiguration {

    @Bean
    ExecutorService testExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}
