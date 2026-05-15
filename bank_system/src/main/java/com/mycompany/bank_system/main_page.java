package com.mycompany.bank_system;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class main_page extends JFrame {
    private final CustomerSession session;
    private final AccountService accountService = new AccountService();
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    private final JLabel balanceLabel = new JLabel("Balance: loading...");
    private final JTextField amountField = new JTextField(12);
    private final JButton depositButton = new JButton("Deposit");
    private final JButton withdrawButton = new JButton("Withdraw");
    private final JButton threadDemoButton = new JButton("Run Thread Demo");
    private final JTextArea activityArea = new JTextArea(10, 40);
    private final Timer autoRefreshTimer;
    private volatile boolean refreshInProgress;

    public main_page(CustomerSession session) {
        this.session = session;
        this.autoRefreshTimer = new Timer(2000, event -> refreshBalance(false));
        initComponents();
        refreshBalance(true);
        autoRefreshTimer.start();
    }

    public main_page() {
        this(new CustomerSession(1, 1, "John Doe", "johndoe", "Savings"));
    }

    private void initComponents() {
        setTitle("ATM Main Page");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        JLabel titleLabel = new JLabel("Welcome, " + session.name());
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        topPanel.add(titleLabel, gbc);

        gbc.gridy++;
        topPanel.add(new JLabel("Account: " + session.accountType() + " #" + session.accountId()), gbc);

        gbc.gridy++;
        topPanel.add(balanceLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        topPanel.add(new JLabel("Amount"), gbc);

        gbc.gridx = 1;
        topPanel.add(amountField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        topPanel.add(depositButton, gbc);

        gbc.gridx = 1;
        topPanel.add(withdrawButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        topPanel.add(threadDemoButton, gbc);

        activityArea.setEditable(false);

        depositButton.addActionListener(event -> submitTransaction("deposit"));
        withdrawButton.addActionListener(event -> submitTransaction("withdrawal"));
        threadDemoButton.addActionListener(event -> runThreadDemo());

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(activityArea), BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    private void submitTransaction(String type) {
        BigDecimal amount;
        try {
            amount = readAmount();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setButtonsEnabled(false);
        appendActivity(type + " started on background thread");

        executor.submit(() -> {
            try {
                BigDecimal newBalance = "deposit".equals(type)
                        ? accountService.deposit(session.accountId(), amount)
                        : accountService.withdraw(session.accountId(), amount);

                SwingUtilities.invokeLater(() -> {
                    balanceLabel.setText("Balance: " + currencyFormat.format(newBalance));
                    amountField.setText("");
                    appendActivity(type + " completed. New balance: " + currencyFormat.format(newBalance));
                    refreshBalance(false);
                    setButtonsEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    appendActivity(type + " failed: " + ex.getMessage());
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Transaction Error",
                            JOptionPane.ERROR_MESSAGE);
                    setButtonsEnabled(true);
                });
            }
        });
    }

    private void runThreadDemo() {
        setButtonsEnabled(false);
        appendActivity("Starting two concurrent transactions. Account lock prevents race conditions.");

        executor.submit(() -> runDemoTransaction("Demo deposit", true, new BigDecimal("100.00")));
        executor.submit(() -> runDemoTransaction("Demo withdrawal", false, new BigDecimal("50.00")));
        executor.submit(() -> {
            sleep(700);
            SwingUtilities.invokeLater(() -> {
                refreshBalance(false);
                setButtonsEnabled(true);
                appendActivity("Thread demo finished. tryLock timeout is used to avoid deadlock waiting.");
            });
        });
    }

    private void runDemoTransaction(String label, boolean deposit, BigDecimal amount) {
        try {
            BigDecimal balance = deposit
                    ? accountService.deposit(session.accountId(), amount)
                    : accountService.withdraw(session.accountId(), amount);
            SwingUtilities.invokeLater(() -> appendActivity(label + " completed: " + currencyFormat.format(balance)));
        } catch (Exception ex) {
            SwingUtilities.invokeLater(() -> appendActivity(label + " failed: " + ex.getMessage()));
        }
    }

    private void refreshBalance(boolean logResult) {
        if (refreshInProgress) {
            return;
        }

        refreshInProgress = true;

        executor.submit(() -> {
            try {
                BigDecimal balance = accountService.getBalance(session.accountId());
                SwingUtilities.invokeLater(() -> {
                    balanceLabel.setText("Balance: " + currencyFormat.format(balance));
                    if (logResult) {
                        appendActivity("Balance loaded: " + currencyFormat.format(balance));
                    }
                    refreshInProgress = false;
                });
            } catch (SQLException ex) {
                SwingUtilities.invokeLater(() -> {
                    balanceLabel.setText("Balance: unavailable");
                    if (logResult) {
                        appendActivity("Balance refresh failed: " + ex.getMessage());
                    }
                    refreshInProgress = false;
                });
            }
        });
    }

    private BigDecimal readAmount() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero.");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Enter a valid amount.");
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        depositButton.setEnabled(enabled);
        withdrawButton.setEnabled(enabled);
        threadDemoButton.setEnabled(enabled);
    }

    private void appendActivity(String message) {
        activityArea.append(message + System.lineSeparator());
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void dispose() {
        autoRefreshTimer.stop();
        executor.shutdownNow();
        super.dispose();
    }
}
