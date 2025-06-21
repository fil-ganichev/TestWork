package ru.ganichev.autonomous;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AutonomousTransactionTest {

    private static final String CACHE_NAME = "testCache";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ExecutorService pool = Executors.newFixedThreadPool(1);
    private final AutonomousTransaction autonomousTransaction = new AutonomousTransaction(pool);
    private Ignite ignite;
    private IgniteCache<String, String> cache;

    @BeforeAll
    void startIgnite() {
        ignite = Ignition.start();

        CacheConfiguration<String, String> cfg = new CacheConfiguration<>();
        cfg.setName(CACHE_NAME);
        cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

        cache = ignite.getOrCreateCache(cfg);
    }

    @AfterAll
    void stopIgnite() {
        Ignition.stop(true);
    }

    @AfterEach
    void clearCache() {
        cache.clear();
    }

    @Test
    void when_mainAndAutonomousTransactionsOk_thenAllDataSaved() {
        try (Transaction mainTransaction = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC,
                TransactionIsolation.REPEATABLE_READ)) {

            log.info("Start main transaction");

            cache.put("someKey1", "someValue1");

            autonomousTransaction.execute(cache, cache -> cache.put("someKey2", "someValue2"));

            log.info("Commit main transaction");
            mainTransaction.commit();
        }
        assertThat(cache.get("someKey1")).isEqualTo("someValue1");
        assertThat(cache.get("someKey2")).isEqualTo("someValue2");
    }

    @Test
    void when_mainTransactionErrorAndAutonomousTransactionsOk_thenAutonomousDataSaved() {
        try (Transaction mainTransaction = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC,
                TransactionIsolation.REPEATABLE_READ)) {

            try {
                log.info("Start main transaction");

                cache.put("someKey1", "someValue1");

                autonomousTransaction.execute(cache, cache -> cache.put("someKey2", "someValue2"));

                throw new RuntimeException("Test: main transaction error");
            } catch (Exception e) {
                mainTransaction.rollback();
            }
        }
        assertThat(cache.containsKey("someKey1")).isFalse();
        assertThat(cache.get("someKey2")).isEqualTo("someValue2");
    }

    @Test
    void when_mainTransactionOkAndAutonomousTransactionsError_thenMainDataSaved() {
        Exception e = null;
        try (Transaction mainTransaction = ignite.transactions().txStart(TransactionConcurrency.PESSIMISTIC,
                TransactionIsolation.REPEATABLE_READ)) {

            log.info("Start main transaction");

            cache.put("someKey1", "someValue1");

            try {
                autonomousTransaction.execute(cache, cache -> {
                    cache.put("someKey2", "someValue2");
                    throw new RuntimeException("Test: autonomous transaction error");
                });
            } catch (Exception ex) {
                e = ex;
            }

            log.info("Commit main transaction");
            mainTransaction.commit();
        }
        assertThat(e).isInstanceOf(AutonomousTransactionException.class);
        assertThat(cache.get("someKey1")).isEqualTo("someValue1");
        assertThat(cache.containsKey("someKey2")).isFalse();
    }
}
