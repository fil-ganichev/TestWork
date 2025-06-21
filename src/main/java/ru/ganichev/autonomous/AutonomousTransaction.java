package ru.ganichev.autonomous;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

public class AutonomousTransaction {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ExecutorService executorService;

    public AutonomousTransaction(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void execute(IgniteCache<String, String> cache, Consumer<IgniteCache<String, String>> action) {
        try {
            executorService.submit(() -> executeInternal(cache, action)).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AutonomousTransactionException(e);
        } catch (ExecutionException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof AutonomousTransactionException) {
                    throw (AutonomousTransactionException) e.getCause();
                }
                throw new AutonomousTransactionException(e.getCause());
            }
            throw new AutonomousTransactionException(e);
        } catch (RejectedExecutionException | NullPointerException e) {
            throw new AutonomousTransactionException(e);
        }
    }

    private void executeInternal(IgniteCache<String, String> cache, Consumer<IgniteCache<String, String>> action) {
        try (Transaction transaction = cache.unwrap(Ignite.class).transactions()
                .txStart(TransactionConcurrency.PESSIMISTIC,
                        TransactionIsolation.SERIALIZABLE)) {

            log.info("Start autonomous transaction");
            try {
                action.accept(cache);
            } catch (Exception e) {
                log.info("Rollback autonomous transaction");
                transaction.rollback();
                throw new AutonomousTransactionException(e);
            }

            log.info("Commit autonomous transaction");
            transaction.commit();
        }
    }
}
