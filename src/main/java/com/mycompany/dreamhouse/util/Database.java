package com.mycompany.dreamhouse.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static final Properties SETTINGS = load();

    private Database() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream input = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
        return properties;
    }

    private static String value(String key, String environmentVariable, String fallback) {
        String environmentValue = System.getenv(environmentVariable);
        return environmentValue == null || environmentValue.isBlank()
                ? SETTINGS.getProperty(key, fallback)
                : environmentValue;
    }

    public static Connection getConnection() throws SQLException {
        String url = value("db.url", "DREAMHOUSE_DB_URL",
                "jdbc:mysql://localhost:3306/dreamhouse_db?serverTimezone=UTC&allowPublicKeyRetrieval=true");
        String username = value("db.username", "DREAMHOUSE_DB_USERNAME", "root");
        String password = value("db.password", "DREAMHOUSE_DB_PASSWORD", "");

        // Explicitly register Connector/J so the webapp works even when the
        // container's JDBC service discovery does not see WEB-INF/lib drivers.
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MySQL Connector/J is missing from the deployed web application. "
                    + "Clean and build the Maven project, then redeploy it to Tomcat.", exception);
        }

        return DriverManager.getConnection(url, username, password);
    }
}
