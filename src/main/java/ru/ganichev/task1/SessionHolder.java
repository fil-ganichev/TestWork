package ru.ganichev.task1;

import static ru.ganichev.task1.Constants.NOT_DEFINED_RATE;

public class SessionHolder {

    private final ThreadLocal<Session> sessions = new ThreadLocal<>();

    public ClassAInterface getClassAInterface() {
        Session session = getOrCreateSession();
        if (session.classAInterface == null) {
            session.classAInterface = new ClassA(this);
            session.classAInterface.postInit();
        }
        return session.classAInterface;
    }

    public ClassBInterface getClassBInterface() {
        Session session = getOrCreateSession();
        if (session.classBInterface == null) {
            session.classBInterface = new ClassB(this);
            session.classBInterface.postInit();
        }
        return session.classBInterface;
    }

    public double getDefaultRate() {
        return getOrCreateSession().defaultRate;
    }

    public void setDefaultRate(double defaultRate) {
        getOrCreateSession().defaultRate = defaultRate;
    }

    private Session getOrCreateSession() {
        Session session = sessions.get();
        if (session == null) {
            session = new Session();
            sessions.set(session);
        }
        return session;
    }

    private static class Session {

        private ClassAInterface classAInterface;
        private ClassBInterface classBInterface;

        private double defaultRate = NOT_DEFINED_RATE;
    }
}
