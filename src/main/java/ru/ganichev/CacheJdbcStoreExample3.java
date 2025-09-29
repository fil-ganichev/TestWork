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
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.query.SqlFieldsQuery;
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
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;

public class CacheJdbcStoreExample3 {

    private static final Logger logger = LoggerFactory.getLogger(CacheJdbcStoreExample3.class);

    /**
     * Cache name.
     */
    private static final String CACHE_NAME1 = "cache1";

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

        try (Ignite ignite = Ignition.start()) {
            logger.info("");
            logger.info(">>> Cache store example started.");

            CacheConfiguration<Long, Person> cacheCfg1 = createConfig(CACHE_NAME1);

            try (IgniteCache<Long, Person> cache1 = ignite.getOrCreateCache(cacheCfg1)) {

                SqlFieldsQuery query = new SqlFieldsQuery("INSERT INTO Person(id, firstName, lastName) VALUES(?, ?, ?)");

                query.setArgs(1L, "firstName", "lastName");

                cache1.query(query);

            } finally {
                ignite.destroyCache(CACHE_NAME1);
            }
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


        QueryEntity queryEntity = new QueryEntity(Long.class, Person.class);
        queryEntity.addQueryField("id", Long.class.getName(), null);
        queryEntity.addQueryField("firstName", String.class.getName(), null);
        queryEntity.addQueryField("lastName", String.class.getName(),null);
        queryEntity.setKeyFieldName("id");
        cacheCfg.setQueryEntities(Collections.singletonList(queryEntity));


        return cacheCfg;
    }
}
