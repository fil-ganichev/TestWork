package ru.ganichev.db;

import javax.sql.DataSource;

public class DataSourceHolder {

    private static DataSource dataSource;

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }
}
