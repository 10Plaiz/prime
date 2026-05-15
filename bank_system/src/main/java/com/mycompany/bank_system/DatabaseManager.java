package com.mycompany.bank_system;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public final class DatabaseManager {
    private static final Path DATABASE_PATH = Paths.get("bank.db");
    private static boolean initialized;

    private DatabaseManager() {
    }

    public static synchronized void initialize() throws SQLException, IOException {
        if (initialized) {
            return;
        }

        try (Connection conn = getConnection()) {
            executeScript(conn, findProjectFile("database.sql"));
            executeScript(conn, findProjectFile("seed_data.sql"));
        }

        initialized = true;
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath());
        try (Statement statement = conn.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public static Optional<CustomerSession> authenticate(String username, String pin) throws SQLException {
        String sql = """
                SELECT c.CustomerID, c.Name, c.Username, a.AccountID, a.Type
                FROM Customer c
                JOIN Account a ON a.CustomerID = c.CustomerID
                WHERE c.Username = ? AND c.Pin = ?
                ORDER BY a.AccountID
                LIMIT 1
                """;

        try (Connection conn = getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, pin);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                return Optional.of(new CustomerSession(
                        rs.getInt("CustomerID"),
                        rs.getInt("AccountID"),
                        rs.getString("Name"),
                        rs.getString("Username"),
                        rs.getString("Type")));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        initialize();
        System.out.println("SQLite database ready at " + DATABASE_PATH.toAbsolutePath());
    }

    private static void executeScript(Connection conn, Path scriptPath) throws IOException, SQLException {
        String script = Files.readString(scriptPath);
        for (String sql : script.split(";")) {
            String trimmed = sql.trim();
            if (!trimmed.isEmpty()) {
                try (Statement statement = conn.createStatement()) {
                    statement.execute(trimmed);
                }
            }
        }
    }

    private static Path findProjectFile(String fileName) throws IOException {
        Path currentDirectory = Paths.get("").toAbsolutePath();
        Path direct = currentDirectory.resolve(fileName);
        if (Files.exists(direct)) {
            return direct;
        }

        Path parent = currentDirectory.getParent();
        if (parent != null) {
            Path parentFile = parent.resolve(fileName);
            if (Files.exists(parentFile)) {
                return parentFile;
            }
        }

        throw new IOException("Cannot find " + fileName + " from " + currentDirectory);
    }
}
