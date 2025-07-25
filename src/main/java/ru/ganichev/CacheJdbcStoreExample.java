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
import ru.ganichev.db.DataSourceHolder;
import ru.ganichev.db.DbH2ServerStartup;
import ru.ganichev.model.Person;
import ru.ganichev.store.CacheJdbcPersonStore;
import ru.ganichev.store.CacheJdbcStoreSessionListener;

import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;

public class CacheJdbcStoreExample {
    /**
     * Cache name.
     */
    private static final String CACHE_NAME = CacheJdbcStoreExample.class.getSimpleName();

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
    public static void main(String[] args) throws IgniteException, SQLException {

        DataSourceHolder.setDataSource(DbH2ServerStartup.populateDatabase());

        // To start ignite with desired configuration uncomment the appropriate line.
        try (Ignite ignite = Ignition.start()) {
            System.out.println();
            System.out.println(">>> Cache store example started.");

            CacheConfiguration<Long, Person> cacheCfg = new CacheConfiguration<>(CACHE_NAME);

            // Set atomicity as transaction, since we are showing transactions in example.
            cacheCfg.setAtomicityMode(TRANSACTIONAL);

            // Configure JDBC store.
            cacheCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(CacheJdbcPersonStore.class));

            // Configure JDBC session listener.
            cacheCfg.setCacheStoreSessionListenerFactories(new Factory<CacheStoreSessionListener>() {
                @Override
                public CacheStoreSessionListener create() {
                    CacheJdbcStoreSessionListener lsnr = new CacheJdbcStoreSessionListener();
                    return lsnr;
                }
            });

            cacheCfg.setReadThrough(true);
            cacheCfg.setWriteThrough(true);

            // Auto-close cache at the end of the example.
            try (IgniteCache<Long, Person> cache = ignite.getOrCreateCache(cacheCfg)) {
                // Make initial cache loading from persistent store. This is a
                // distributed operation and will call CacheStore.loadCache(...)
                // method on all nodes in topology.
                loadCache(cache);

                // Start transaction and execute several cache operations with
                // read/write-through to persistent store.
                executeTransaction(cache);
            } finally {
                // Distributed cache could be removed from cluster only by #destroyCache() call.
                ignite.destroyCache(CACHE_NAME);
            }
        }
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

        System.out.println(">>> Loaded " + cache.size() + " keys with backups in " + (end - start) + "ms.");
    }

    /**
     * Executes transaction with read/write-through to persistent store.
     *
     * @param cache Cache to execute transaction on.
     */
    private static void executeTransaction(IgniteCache<Long, Person> cache) {
        try (Transaction tx = Ignition.ignite().transactions().txStart()) {
            Person val = cache.get(id);

            System.out.println("Read value: " + val);

            val = cache.getAndPut(id, new Person(id, "Isaac", "Newton"));

            System.out.println("Overwrote old value: " + val);

            val = cache.get(id);

            System.out.println("Read value: " + val);

            tx.commit();
        }

        System.out.println("Read value after commit: " + cache.get(id));

        // Clear entry from memory, but keep it in store.
        cache.clear(id);

        // Operations on this cache will not affect store.
        IgniteCache<Long, Person> cacheSkipStore = cache.withSkipStore();

        System.out.println("Read value skipping store (expecting null): " + cacheSkipStore.get(id));

        System.out.println("Read value with store lookup (expecting NOT null): " + cache.get(id));

        // Expecting not null, since entry should be in memory since last call.
        System.out.println("Read value skipping store (expecting NOT null): " + cacheSkipStore.get(id));
    }
}
