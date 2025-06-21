package ru.ganichev.autonomous;

public class AutonomousTransactionException extends RuntimeException {

    public AutonomousTransactionException(Throwable exception) {
        super(exception);
    }
}
