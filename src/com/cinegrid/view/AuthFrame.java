package com.cinegrid.view;

import com.cinegrid.config.DBConfig;
import com.cinegrid.util.SecurityUtil;
import com.cinegrid.util.TransitionLoader;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.util.regex.Pattern;

public class AuthFrame extends JFrame {
    private JTextField txtEmail, txtName; 
    private JPasswordField txtPassword; 
    private JComboBox<String> comboRole;
    private JButton btnLogin, btnSwitchMode;
    private boolean isLoginMode = true;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public AuthFrame() {
        setTitle("CineGrid Gatekeeper Panel - Secure Portal"); 
        setSize(520, 480); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        getContentPane().setBackground(new Color(18, 18, 18)); 
        setLayout(new GridBagLayout());
        
        toggleAuthUI();
    }

    private void toggleAuthUI() {
        getContentPane().removeAll();
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.insets = new Insets(12, 15, 12, 15); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Header
        JLabel title = new JLabel(isLoginMode ? "SECURE LOGIN GATEWAY" : "CREATE NEW ACCOUNT", SwingConstants.CENTER); 
        title.setFont(new Font("SansSerif", Font.BOLD, 22)); 
        title.setForeground(new Color(248, 68, 100));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; add(title, gbc); gbc.gridwidth = 1;

        int row = 1;

        // Full Name Field (Signup Mode Only)
        if (!isLoginMode) {
            JLabel lblN = new JLabel("Full Name:"); 
            lblN.setForeground(new Color(200, 200, 200));
            lblN.setFont(new Font("SansSerif", Font.BOLD, 13));
            gbc.gridx = 0; gbc.gridy = row; add(lblN, gbc);
            
            txtName = new JTextField(20); 
            styleTextField(txtName);
            gbc.gridx = 1; add(txtName, gbc);
            
            // Enter key mapping for smooth keyboard flow
            txtName.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) txtEmail.requestFocusInWindow();
                }
            });
            row++;
        }

        // Email Field
        JLabel lblE = new JLabel("Email Address:"); 
        lblE.setForeground(new Color(200, 200, 200));
        lblE.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = row; add(lblE, gbc);
        
        txtEmail = new JTextField(20); 
        styleTextField(txtEmail);
        gbc.gridx = 1; add(txtEmail, gbc);
        
        txtEmail.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) txtPassword.requestFocusInWindow();
            }
        });
        row++;

        // Password Field
        JLabel lblP = new JLabel("Password:"); 
        lblP.setForeground(new Color(200, 200, 200));
        lblP.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = row; add(lblP, gbc);
        
        txtPassword = new JPasswordField(20); 
        styleTextField(txtPassword);
        gbc.gridx = 1; add(txtPassword, gbc);
        
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) comboRole.requestFocusInWindow();
            }
        });
        row++;

        // Role Selector ComboBox
        JLabel lblR = new JLabel("Select System Role:"); 
        lblR.setForeground(new Color(200, 200, 200));
        lblR.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = row; add(lblR, gbc);
        
        comboRole = new JComboBox<>(new String[]{"CUSTOMER", "ADMIN"});
        comboRole.setFont(new Font("SansSerif", Font.BOLD, 14));
        comboRole.setBackground(new Color(40, 40, 40));
        comboRole.setForeground(Color.WHITE);
        comboRole.setPreferredSize(new Dimension(240, 38));
        gbc.gridx = 1; add(comboRole, gbc);
        
        comboRole.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) btnLogin.doClick();
            }
        });
        row++;

        // Main Submit Action Button
        btnLogin = new JButton(isLoginMode ? "Access Secure Gateway" : "Register Credentials"); 
        btnLogin.setBackground(new Color(248, 68, 100)); 
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(400, 45));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; add(btnLogin, gbc); gbc.gridwidth = 1;
        row++;

        // Switch Mode Link Button
        btnSwitchMode = new JButton(isLoginMode ? "Don't have an account? Sign Up" : "Already have an account? Login");
        btnSwitchMode.setForeground(new Color(0, 200, 255)); 
        btnSwitchMode.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnSwitchMode.setContentAreaFilled(false); 
        btnSwitchMode.setBorderPainted(false);
        btnSwitchMode.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; add(btnSwitchMode, gbc);

        // Action Listeners
        btnLogin.addActionListener(e -> { if (isLoginMode) handleLogin(); else handleSignup(); });
        btnSwitchMode.addActionListener(e -> { isLoginMode = !isLoginMode; toggleAuthUI(); });

        // Default initial focus setup for keyboard navigation
        SwingUtilities.invokeLater(() -> {
            if (!isLoginMode && txtName != null) txtName.requestFocusInWindow();
            else if (txtEmail != null) txtEmail.requestFocusInWindow();
        });

        revalidate(); repaint();
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(240, 38));
        tf.setBackground(new Color(35, 35, 35));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(new LineBorder(new Color(60, 60, 60), 1, true));
    }

    private void handleLogin() {
        String em = txtEmail.getText().trim(); 
        String pass = new String(txtPassword.getPassword());
        String role = comboRole.getSelectedItem().toString();
        
        if (!EMAIL_PATTERN.matcher(em).matches()) { 
            JOptionPane.showMessageDialog(this, "Invalid email format entered!", "Validation Error", JOptionPane.WARNING_MESSAGE); 
            txtEmail.requestFocusInWindow();
            return; 
        }
        
        String hs = SecurityUtil.hashPassword(pass);
        try (Connection c = DBConfig.getConnection(); 
             PreparedStatement p = c.prepareStatement("SELECT id, name FROM users WHERE email=? AND password_hash=? AND role=?")) {
            p.setString(1, em); p.setString(2, hs); p.setString(3, role);
            ResultSet rs = p.executeQuery();
            if (rs.next()) {
                int fetchedUserId = rs.getInt("id");
                String fetchedUserName = rs.getString("name");
                
                // Triggering the premium transition loader before opening Dashboard
                TransitionLoader.showTransition(this, "Authenticating Secure Gateway...", () -> {
                    JOptionPane.showMessageDialog(this, "Welcome " + fetchedUserName, "Login Success", JOptionPane.INFORMATION_MESSAGE);
                    new DashboardFrame(fetchedUserId, fetchedUserName, role).setVisible(true); 
                    this.dispose();
                });
            } else { 
                JOptionPane.showMessageDialog(this, "Invalid credentials or chosen role mismatch!", "Authentication Failed", JOptionPane.ERROR_MESSAGE); 
            }
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "System Exception", JOptionPane.ERROR_MESSAGE); 
        }
    }

    private void handleSignup() {
        String name = txtName.getText().trim(); 
        String em = txtEmail.getText().trim(); 
        String pass = new String(txtPassword.getPassword()); 
        String role = comboRole.getSelectedItem().toString();
        
        if (name.isEmpty() || em.isEmpty() || pass.length() < 6) { 
            JOptionPane.showMessageDialog(this, "Fields are empty or password is less than 6 characters!", "Validation Error", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        String hs = SecurityUtil.hashPassword(pass);
        
        try (Connection c = DBConfig.getConnection(); 
             PreparedStatement p = c.prepareStatement("INSERT INTO users (name, email, password_hash, role) VALUES (?, ?, ?, ?)")) {
            p.setString(1, name); p.setString(2, em); p.setString(3, hs); p.setString(4, role);
            p.executeUpdate();
            
            // Transition loader for smooth signup feedback transition
            TransitionLoader.showTransition(this, "Registering Credentials...", () -> {
                JOptionPane.showMessageDialog(this, "Account Created Successfully! Please Login.", "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                isLoginMode = true; 
                toggleAuthUI();
            });
        } catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Signup Failed (Duplicate Email): " + ex.getMessage(), "Database Rejection", JOptionPane.ERROR_MESSAGE); 
        }
    }
}