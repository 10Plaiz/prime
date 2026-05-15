package com.mycompany.bank_system;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Login extends JFrame {
    private static final Logger logger = Logger.getLogger(Login.class.getName());

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField pinField = new JPasswordField(18);
    private final JButton loginButton = new JButton("Login");

    public Login() {
        initComponents();
    }

    private void initComponents() {
        setTitle("ATM Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("ATM LOGIN");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 20f));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        panel.add(new JLabel("Username"), gbc);

        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Passcode"), gbc);

        gbc.gridx = 1;
        panel.add(pinField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        panel.add(loginButton, gbc);

        loginButton.addActionListener(event -> login());
        pinField.addActionListener(event -> login());

        add(panel);
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (username.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and passcode.", "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        loginButton.setEnabled(false);

        Thread loginThread = new Thread(() -> {
            try {
                DatabaseManager.initialize();
                Optional<CustomerSession> session = DatabaseManager.authenticate(username, pin);

                SwingUtilities.invokeLater(() -> {
                    if (session.isPresent()) {
                        new main_page(session.get()).setVisible(true);
                        dispose();
                    } else {
                        loginButton.setEnabled(true);
                        JOptionPane.showMessageDialog(this, "Invalid username or passcode.", "Login Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Login failed", ex);
                SwingUtilities.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }, "login-worker");

        loginThread.start();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new Login().setVisible(true));
    }
}
