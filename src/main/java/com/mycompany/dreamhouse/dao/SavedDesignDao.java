package com.mycompany.dreamhouse.dao;

import com.mycompany.dreamhouse.model.SavedDesign;
import com.mycompany.dreamhouse.util.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SavedDesignDao {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS saved_designs ("
            + "id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,"
            + "user_id BIGINT UNSIGNED NOT NULL,"
            + "category VARCHAR(40) NOT NULL,"
            + "width_ft DECIMAL(7,2) NOT NULL,"
            + "length_ft DECIMAL(7,2) NOT NULL,"
            + "design_name VARCHAR(160) NOT NULL,"
            + "description VARCHAR(255) NOT NULL,"
            + "items_json JSON NOT NULL,"
            + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
            + "PRIMARY KEY (id),"
            + "KEY ix_saved_designs_user_created (user_id, created_at),"
            + "CONSTRAINT fk_saved_designs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            + ") ENGINE=InnoDB";

    public long save(long userId, String category, double width, double length,
            String name, String description, String itemsJson) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            ensureTable(connection);
            String sql = "INSERT INTO saved_designs(user_id,category,width_ft,length_ft,design_name,description,items_json) "
                    + "VALUES(?,?,?,?,?,?,?)";
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, userId);
                statement.setString(2, category);
                statement.setDouble(3, width);
                statement.setDouble(4, length);
                statement.setString(5, name);
                statement.setString(6, description);
                statement.setString(7, itemsJson);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) return keys.getLong(1);
                }
                throw new SQLException("The saved design ID was not returned.");
            }
        }
    }

    public List<SavedDesign> findForUser(long userId) throws SQLException {
        List<SavedDesign> designs = new ArrayList<>();
        try (Connection connection = Database.getConnection()) {
            ensureTable(connection);
            String sql = "SELECT id,category,width_ft,length_ft,design_name,description,items_json,created_at "
                    + "FROM saved_designs WHERE user_id=? ORDER BY created_at DESC,id DESC";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, userId);
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        Timestamp createdAt = rows.getTimestamp("created_at");
                        designs.add(new SavedDesign(rows.getLong("id"), rows.getString("category"),
                                rows.getDouble("width_ft"), rows.getDouble("length_ft"),
                                rows.getString("design_name"), rows.getString("description"),
                                rows.getString("items_json"), createdAt == null ? "" : createdAt.toString()));
                    }
                }
            }
        }
        return designs;
    }

    public boolean delete(long userId, long designId) throws SQLException {
        try (Connection connection = Database.getConnection()) {
            ensureTable(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM saved_designs WHERE id=? AND user_id=?")) {
                statement.setLong(1, designId);
                statement.setLong(2, userId);
                return statement.executeUpdate() > 0;
            }
        }
    }

    private static void ensureTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_TABLE);
        }
    }
}
