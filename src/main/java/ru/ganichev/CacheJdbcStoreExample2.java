/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ganichev;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.store.CacheStoreSessionListener;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ganichev.db.DataSourceHolder;
import ru.ganichev.db.DbH2ServerStartup;
import ru.ganichev.model.Person;
import ru.ganichev.store.CacheJdbcPersonStore;
import ru.ganichev.store.CacheJdbcStoreSessionListener;

import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;

public class CacheJdbcStoreExample2 {

    private static final Logger logger = LoggerFactory.getLogger(CacheJdbcStoreExample2.class);

    /**
     * Cache name.
     */
    private static final String CACHE_NAME1 = "cache1";
    private static final String CACHE_NAME2 = "cache2";

    /**
     * Number of entries to load.
     */
    private static final int ENTRY_COUNT = 100_000;

    /**
     * Global person ID to use across entire example.
     */
    private static final Long id = Math.abs(UUID.randomUUID().getLeastSignificantBits());

    /**
     * Executes example.
     *
     * @param args Command line arguments, none required.
     * @throws IgniteException If example execution failed.
     */
    public static void main(String[] args) throws IgniteException, SQLException, InterruptedException {

        DataSourceHolder.setDataSource(DbH2ServerStartup.populateDatabase());

        // To start ignite with desired configuration uncomment the appropriate line.
        try (Ignite ignite = Ignition.start()) {
            logger.info("");
            logger.info(">>> Cache store example started.");

            CacheConfiguration<Long, Person> cacheCfg1 = createConfig(CACHE_NAME1);
            CacheConfiguration<Long, Person> cacheCfg2 = createConfig(CACHE_NAME2);

            // Auto-close cache at the end of the example.
            try (IgniteCache<Long, Person> cache1 = ignite.getOrCreateCache(cacheCfg1);
                 IgniteCache<Long, Person> cache2 = ignite.getOrCreateCache(cacheCfg2)) {
                // Make initial cache loading from persistent store. This is a
                // distributed operation and will call CacheStore.loadCache(...)
                // method on all nodes in topology.
                loadCache(cache1);
                loadCache(cache2);

                final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
                // Start transaction and execute several cache operations with
                // read/write-through to persistent store.
                Thread t1 = new Thread(() -> {
                    doWait(cyclicBarrier);
                    executeTransaction(cache1, cache2);
                }, "TX1");
                Thread t2 = new Thread(() -> {
                    doWait(cyclicBarrier);
                    executeTransaction(cache1, cache2);
                }, "TX2");

                t1.start();
                t2.start();

                t1.join();
                t2.join();
                //executeTransaction(cache1, cache2);
            } finally {
                // Distributed cache could be removed from cluster only by #destroyCache() call.
                ignite.destroyCache(CACHE_NAME1);
                ignite.destroyCache(CACHE_NAME2);
            }
        }
    }

    private static void doWait(CyclicBarrier cyclicBarrier) {
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
        }
    }

    private static CacheConfiguration<Long, Person> createConfig(String name) {
        CacheConfiguration<Long, Person> cacheCfg = new CacheConfiguration<>(name);

        // Set atomicity as transaction, since we are showing transactions in example.
        cacheCfg.setAtomicityMode(TRANSACTIONAL);

        // Configure JDBC store.
        cacheCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(CacheJdbcPersonStore.class));

        // Configure JDBC session listener.
        cacheCfg.setCacheStoreSessionListenerFactories((Factory<CacheStoreSessionListener>) () -> {
            CacheJdbcStoreSessionListener lsnr = new CacheJdbcStoreSessionListener();
            return lsnr;
        });

        cacheCfg.setReadThrough(true);
        cacheCfg.setWriteThrough(true);

        return cacheCfg;
    }

    /**
     * Makes initial cache loading.
     *
     * @param cache Cache to load.
     */
    private static void loadCache(IgniteCache<Long, Person> cache) {
        long start = System.currentTimeMillis();

        // Start loading cache from persistent store on all caching nodes.
        cache.loadCache(null, ENTRY_COUNT);

        long end = System.currentTimeMillis();

        logger.info(">>> Loaded " + cache.size() + " keys with backups in " + (end - start) + "ms.");
    }


    private static void executeTransaction(IgniteCache<Long, Person> cache1, IgniteCache<Long, Person> cache2) {
        try (Transaction tx = Ignition.ignite().transactions().txStart(TransactionConcurrency.OPTIMISTIC,
                TransactionIsolation.READ_COMMITTED)) {
            logger.info("Start transaction");

            Person val = cache1.get(id);

            logger.info("Read value: " + val);

            val = cache1.getAndPut(id, new Person(id, "Isaac", "Newton"));

            logger.info("Overwrote old value: " + val);

            val = cache1.get(id);

            logger.info("Read value: " + val);

            long key = id + 1;

            val = cache2.get(id);

            logger.info("Read value: " + val);

            val = cache2.getAndPut(key, new Person(key, "Isaac", "Newton"));

            logger.info("Overwrote old value: " + val);

            val = cache2.get(key);

            logger.info("Read value: " + val);

            logger.info("Before commit");

            tx.commit();
        }
    }
}
