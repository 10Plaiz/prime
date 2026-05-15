package com.mycompany.bank_system;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class AccountService {
    private static final Map<Integer, ReentrantLock> ACCOUNT_LOCKS = new ConcurrentHashMap<>();

    public BigDecimal getBalance(int accountId) throws SQLException {
        String sql = "SELECT Balance FROM Account WHERE AccountID = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, accountId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Account not found: " + accountId);
                }

                return rs.getBigDecimal("Balance");
            }
        }
    }

    public BigDecimal deposit(int accountId, BigDecimal amount) throws SQLException, InterruptedException {
        return changeBalance(accountId, amount, "deposit");
    }

    public BigDecimal withdraw(int accountId, BigDecimal amount) throws SQLException, InterruptedException {
        return changeBalance(accountId, amount.negate(), "withdrawal");
    }

    private BigDecimal changeBalance(int accountId, BigDecimal delta, String transactionType)
            throws SQLException, InterruptedException {
        if (delta.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        ReentrantLock lock = ACCOUNT_LOCKS.computeIfAbsent(accountId, id -> new ReentrantLock(true));
        if (!lock.tryLock(3, TimeUnit.SECONDS)) {
            throw new IllegalStateException("Operation timed out waiting for the account lock. Deadlock avoided.");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                BigDecimal currentBalance = readBalanceForUpdate(conn, accountId);
                BigDecimal newBalance = currentBalance.add(delta);

                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Insufficient balance.");
                }

                updateBalance(conn, accountId, newBalance);
                insertTransaction(conn, accountId, transactionType, delta.abs());
                conn.commit();
                return newBalance;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } finally {
            lock.unlock();
        }
    }

    private BigDecimal readBalanceForUpdate(Connection conn, int accountId) throws SQLException {
        String sql = "SELECT Balance FROM Account WHERE AccountID = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, accountId);

            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Account not found: " + accountId);
                }

                return rs.getBigDecimal("Balance");
            }
        }
    }

    private void updateBalance(Connection conn, int accountId, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE Account SET Balance = ? WHERE AccountID = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setBigDecimal(1, newBalance);
            statement.setInt(2, accountId);
            statement.executeUpdate();
        }
    }

    private void insertTransaction(Connection conn, int accountId, String transactionType, BigDecimal amount)
            throws SQLException {
        String sql = "INSERT INTO Transactions (AccountID, Type, Amount) VALUES (?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            statement.setString(2, transactionType);
            statement.setBigDecimal(3, amount);
            statement.executeUpdate();
        }
    }
}
