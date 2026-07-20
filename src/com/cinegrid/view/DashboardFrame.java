package com.cinegrid.view;

import com.cinegrid.config.DBConfig;
import com.cinegrid.dao.BookingDAO;
import com.cinegrid.util.SecurityUtil;
import com.cinegrid.util.TransitionLoader;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class DashboardFrame extends JFrame {
    private int userId; 
    String userName; 
    private String userRole;
    private JPanel mainContentPanel; 
    private JTextField txtSearch; 
    private JComboBox<String> comboGenre;
    private JComboBox<String> comboFormatFilter;
    private BookingDAO bookingDAO = new BookingDAO();
    private JComboBox<String> comboAdminMovies;
    private JTabbedPane tabbedPane;
    
    private JButton btnUploadBulk;
    private JButton btnAddShow;
    private JButton btnSaveManualPoster;
    private JButton btnLoad;
    private JButton btnWipeOut;
    
    private JComboBox<String> comboManualInsertMovies;
    private JTextField txtManualPosterUrl;

    public DashboardFrame(int userId, String userName, String userRole) {
        this.userId = userId; 
        this.userName = userName; 
        this.userRole = userRole;
        
        setTitle("CineGrid Enterprise System Portal"); 
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        getContentPane().setBackground(new Color(15, 15, 15)); 
        setLayout(new BorderLayout());
        
        // --- HEADER BAR WITH LOGOUT BUTTON & LIVE CLOCK TIMER ---
        JPanel header = new JPanel(new BorderLayout()); 
        header.setBackground(new Color(24, 24, 24)); 
        header.setPreferredSize(new Dimension(1350, 80));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(40, 40, 40)));
        
        JLabel logo = new JLabel("   CineGrid", SwingConstants.LEFT); 
        logo.setFont(new Font("SansSerif", Font.BOLD, 32)); 
        logo.setForeground(new Color(248, 68, 100));
        
        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 18));
        rightHeaderPanel.setBackground(new Color(24, 24, 24));
        
        JLabel info = new JLabel("User Session: " + userName + " [" + userRole + "]    "); 
        info.setForeground(Color.LIGHT_GRAY); 
        info.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // 🕒 Live Real-Time Clock Timer (Top Right Section, NO auto-logout)
        JLabel lblLiveTimer = new JLabel();
        lblLiveTimer.setForeground(new Color(0, 230, 115));
        lblLiveTimer.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // Swing Timer to update clock every 1 second
        Timer clockTimer = new Timer(1000, e -> {
            DateTimeFormatter clockFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a");
            lblLiveTimer.setText("🕒 " + LocalDateTime.now().format(clockFormatter) + "    ");
        });
        clockTimer.start();

        JButton btnLogout = new JButton("🚪 Logout");
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setPreferredSize(new Dimension(110, 36));
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Kya aap waqai logout karna chahte hain?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                clockTimer.stop(); // Stop clock timer on session termination
                TransitionLoader.showTransition(this, "Terminating Session...", () -> {
                    new AuthFrame().setVisible(true);
                    this.dispose();
                });
            }
        });

        rightHeaderPanel.add(lblLiveTimer);
        rightHeaderPanel.add(info);
        rightHeaderPanel.add(btnLogout);

        header.add(logo, BorderLayout.WEST); 
        header.add(rightHeaderPanel, BorderLayout.EAST); 
        add(header, BorderLayout.NORTH);

        // Ultra Premium Styled Tabs Configure Engine
        tabbedPane = new JTabbedPane(); 
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 15));
        tabbedPane.setBackground(new Color(28, 28, 28));
        tabbedPane.setForeground(Color.WHITE);
        
        JPanel moviePanel = new JPanel(new BorderLayout()); 
        moviePanel.setBackground(new Color(15, 15, 15));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15)); 
        filterPanel.setBackground(new Color(24, 24, 24));
        
        txtSearch = new JTextField(20); 
        txtSearch.setPreferredSize(new Dimension(280, 38));
        txtSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtSearch.setBackground(new Color(35, 35, 35));
        txtSearch.setForeground(Color.WHITE);
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setBorder(new LineBorder(new Color(60, 60, 60), 1));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) { 
                loadMoviesCatalogueFiltered(txtSearch.getText().trim(), comboGenre.getSelectedItem().toString(), comboFormatFilter.getSelectedItem().toString()); 
            }
        });
        
        comboGenre = new JComboBox<>(new String[]{"All Genres", "Action/Adventure/Sci-Fi", "Adventure/Comedy", "Action/Drama/Fantasy", "Horror/Supernatural", "Romance/Drama", "Thriller/Mystery", "Animation/Family"});
        comboGenre.setFont(new Font("SansSerif", Font.BOLD, 13));
        comboGenre.setPreferredSize(new Dimension(190, 38));
        comboGenre.addActionListener(e -> loadMoviesCatalogueFiltered(txtSearch.getText().trim(), comboGenre.getSelectedItem().toString(), comboFormatFilter.getSelectedItem().toString()));
        
        comboFormatFilter = new JComboBox<>(new String[]{"All Formats", "IMAX", "4DX", "3D", "2D"});
        comboFormatFilter.setFont(new Font("SansSerif", Font.BOLD, 13));
        comboFormatFilter.setPreferredSize(new Dimension(130, 38));
        comboFormatFilter.addActionListener(e -> loadMoviesCatalogueFiltered(txtSearch.getText().trim(), comboGenre.getSelectedItem().toString(), comboFormatFilter.getSelectedItem().toString()));

        JLabel lblSearchTitle = new JLabel("Search Movies (Ctrl+S):");
        lblSearchTitle.setForeground(Color.WHITE);
        lblSearchTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        filterPanel.add(lblSearchTitle); 
        filterPanel.add(txtSearch);
        filterPanel.add(comboGenre); 
        filterPanel.add(comboFormatFilter);
        moviePanel.add(filterPanel, BorderLayout.NORTH);

        mainContentPanel = new JPanel(new GridLayout(0, 4, 25, 25)); 
        mainContentPanel.setBackground(new Color(15, 15, 15));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JScrollPane scroll = new JScrollPane(mainContentPanel); 
        scroll.getVerticalScrollBar().setUnitIncrement(25); 
        scroll.setBorder(null);
        moviePanel.add(scroll, BorderLayout.CENTER);

        addCustomTab("🎬 Recommended Movies", moviePanel);
        addCustomTab("👤 Manage Profile", createProfilePanel());

        if (userRole.equalsIgnoreCase("CUSTOMER")) {
            addCustomTab("📜 Purchase History", createCustomerProfilePanel());
        } else {
            addCustomTab("🛠️ Admin Console Panel (CRUD)", createAdminPanel());
            addCustomTab("🎫 All User Bookings Audit", createAdminAllBookingsPanel());
        }

        add(tabbedPane, BorderLayout.CENTER);
        setupKeyboardShortcuts();
        loadMoviesCatalogueFiltered("", "All Genres", "All Formats");
    }

    private void addCustomTab(String title, JPanel panel) {
        tabbedPane.addTab(title, panel);
        int index = tabbedPane.getTabCount() - 1;
        JLabel lblTab = new JLabel(title);
        lblTab.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTab.setForeground(Color.WHITE);
        lblTab.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); 
        tabbedPane.setTabComponentAt(index, lblTab);
    }

    private void setupKeyboardShortcuts() {
        JComponent root = getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), "focusSearch");
        actionMap.put("focusSearch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(0);
                txtSearch.requestFocusInWindow();
                txtSearch.selectAll();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, ActionEvent.CTRL_MASK), "nextTab");
        actionMap.put("nextTab", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int next = (tabbedPane.getSelectedIndex() + 1) % tabbedPane.getTabCount();
                tabbedPane.setSelectedIndex(next);
            }
        });

        if (!userRole.equalsIgnoreCase("CUSTOMER")) {
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK), "triggerUpload");
            actionMap.put("triggerUpload", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tabbedPane.setSelectedIndex(2);
                    if (btnUploadBulk != null && btnUploadBulk.isEnabled()) btnUploadBulk.doClick();
                }
            });

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK), "triggerAddShow");
            actionMap.put("triggerAddShow", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tabbedPane.setSelectedIndex(2);
                    if (btnAddShow != null) btnAddShow.doClick();
                }
            });

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK), "triggerPosterSave");
            actionMap.put("triggerPosterSave", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tabbedPane.setSelectedIndex(2);
                    if (btnSaveManualPoster != null) btnSaveManualPoster.doClick();
                }
            });

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK), "triggerRefresh");
            actionMap.put("triggerRefresh", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (btnLoad != null) btnLoad.doClick();
                }
            });

            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK), "triggerWipeOut");
            actionMap.put("triggerWipeOut", new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tabbedPane.setSelectedIndex(2);
                    if (btnWipeOut != null) btnWipeOut.doClick();
                }
            });
        }
    }

    private ImageIcon generateFallbackPoster(String title, String genre) {
        int width = 240; int height = 320;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Color gradientStart = new Color(45, 45, 45); Color gradientEnd = new Color(25, 25, 25);
        if (genre.contains("Action")) gradientStart = new Color(140, 25, 40);
        else if (genre.contains("Horror")) gradientStart = new Color(55, 25, 85);
        else if (genre.contains("Comedy")) gradientStart = new Color(25, 95, 135);
        else if (genre.contains("Thriller")) gradientStart = new Color(95, 75, 25);
        else if (genre.contains("Romance")) gradientStart = new Color(130, 35, 95);
        
        GradientPaint gp = new GradientPaint(0, 0, gradientStart, 0, height, gradientEnd);
        g2.setPaint(gp); g2.fillRect(0, 0, width, height);
        g2.setColor(new Color(248, 68, 100, 180)); g2.setStroke(new BasicStroke(4)); g2.drawRect(8, 8, width - 16, height - 16);
        g2.setColor(new Color(255, 255, 255, 25)); g2.setFont(new Font("SansSerif", Font.BOLD, 90)); g2.drawString("🎬", width / 2 - 45, height / 2 - 20);
        g2.setColor(Color.WHITE); g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        String[] words = title.split(" "); int y = height / 2 + 40; StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (fm.stringWidth(currentLine.toString() + word) < width - 40) { currentLine.append(word).append(" "); }
            else { g2.drawString(currentLine.toString().trim(), (width - fm.stringWidth(currentLine.toString().trim())) / 2, y); y += 24; currentLine = new StringBuilder(word).append(" "); }
        }
        if (currentLine.length() > 0) { g2.drawString(currentLine.toString().trim(), (width - fm.stringWidth(currentLine.toString().trim())) / 2, y); }
        g2.setColor(new Color(255, 255, 255, 150)); g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        fm = g2.getFontMetrics(); String tag = "CINEGRID ENGINE SOURCE"; g2.drawString(tag, (width - fm.stringWidth(tag)) / 2, 35);
        g2.dispose(); return new ImageIcon(img);
    }

    private void loadMoviesCatalogueFiltered(String query, String genreFilter, String formatFilter) {
        mainContentPanel.removeAll();
        String sql = "SELECT * FROM movies WHERE 1=1";
        if (!query.isEmpty()) sql += " AND title LIKE ?";
        if (!genreFilter.equals("All Genres")) sql += " AND genre = ?";
        if (!formatFilter.equals("All Formats")) sql += " AND format_type = ?";

        try (Connection conn = DBConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int idx = 1;
            if (!query.isEmpty()) pstmt.setString(idx++, "%" + query + "%");
            if (!genreFilter.equals("All Genres")) pstmt.setString(idx++, genreFilter);
            if (!formatFilter.equals("All Formats")) pstmt.setString(idx++, formatFilter);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int mid = rs.getInt("id"); 
                String title = rs.getString("title"); 
                String genre = rs.getString("genre");
                String fmt = rs.getString("format_type"); 
                String cert = rs.getString("certification");
                double rat = rs.getDouble("rating"); 
                String likes = rs.getString("likes_count");
                String pUrl = rs.getString("poster_url");

                JPanel card = new JPanel(); 
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBackground(new Color(28, 28, 28)); 
                card.setBorder(new LineBorder(new Color(45, 45, 45), 1, true));
                
                JLabel posterLabel = new JLabel();
                posterLabel.setPreferredSize(new Dimension(240, 320)); 
                posterLabel.setMaximumSize(new Dimension(240, 320));
                posterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                posterLabel.setIcon(generateFallbackPoster(title, genre));
                
                if (pUrl != null && pUrl.trim().startsWith("http")) {
                    new Thread(() -> {
                        try {
                            URL url = new URL(pUrl);
                            HttpURLConnection connHttp = (HttpURLConnection) url.openConnection();
                            connHttp.setRequestProperty("User-Agent", "Mozilla/5.0");
                            connHttp.setConnectTimeout(3000);
                            connHttp.connect();
                            if (connHttp.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                try (InputStream in = connHttp.getInputStream()) {
                                    BufferedImage bufferedImg = javax.imageio.ImageIO.read(in);
                                    if (bufferedImg != null) {
                                        Image img = bufferedImg.getScaledInstance(240, 320, Image.SCALE_SMOOTH);
                                        SwingUtilities.invokeLater(() -> posterLabel.setIcon(new ImageIcon(img)));
                                    }
                                }
                            }
                        } catch (Exception ignored) {}
                    }).start();
                }

                JLabel lblLike = new JLabel(" ★ " + rat + "/10  (" + likes + ")"); 
                lblLike.setForeground(new Color(0, 230, 115)); 
                lblLike.setFont(new Font("SansSerif", Font.BOLD, 13));
                lblLike.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                JLabel lblTitle = new JLabel(title); 
                lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16)); 
                lblTitle.setForeground(Color.WHITE); 
                lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                JLabel lblMeta = new JLabel(fmt + " | " + cert + " | " + genre); 
                lblMeta.setForeground(Color.LIGHT_GRAY); 
                lblMeta.setFont(new Font("SansSerif", Font.PLAIN, 12));
                lblMeta.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                JButton btnBook = new JButton("Book Tickets"); 
                btnBook.setFont(new Font("SansSerif", Font.BOLD, 14));
                btnBook.setBackground(new Color(248, 68, 100)); 
                btnBook.setForeground(Color.WHITE); 
                btnBook.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnBook.setFocusPainted(true);
                btnBook.addActionListener(e -> openScheduleSelector(mid, title));

                card.add(Box.createRigidArea(new Dimension(0, 12))); card.add(posterLabel);
                card.add(Box.createRigidArea(new Dimension(0, 8))); card.add(lblLike);
                card.add(Box.createRigidArea(new Dimension(0, 5))); card.add(lblTitle);
                card.add(Box.createRigidArea(new Dimension(0, 5))); card.add(lblMeta);
                card.add(Box.createRigidArea(new Dimension(0, 12))); card.add(btnBook);
                card.add(Box.createRigidArea(new Dimension(0, 10)));
                
                mainContentPanel.add(card);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error encountered while loading movies catalog: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        mainContentPanel.revalidate(); 
        mainContentPanel.repaint();
    }
    
    private void openScheduleSelector(int movieId, String title) {
        JDialog dial = new JDialog(this, "Select Show Time - " + title, true); 
        dial.setSize(550, 420); 
        dial.setLocationRelativeTo(this); 
        dial.setLayout(new BoxLayout(dial.getContentPane(), BoxLayout.Y_AXIS));
        dial.getContentPane().setBackground(new Color(24, 24, 24));
        
        dial.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        dial.getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { dial.dispose(); }
        });
        
        JLabel head = new JLabel("Available Show Timings for " + title + " (Esc to close)");
        head.setForeground(Color.WHITE); head.setFont(new Font("SansSerif", Font.BOLD, 16));
        head.setAlignmentX(Component.CENTER_ALIGNMENT);
        dial.add(Box.createRigidArea(new Dimension(0, 20))); dial.add(head);
        dial.add(Box.createRigidArea(new Dimension(0, 20)));

        boolean hasShows = false;
        try (Connection conn = DBConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id, show_date, show_time, ticket_price FROM shows WHERE movie_id=?")) {
            ps.setInt(1, movieId); 
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                hasShows = true;
                int sid = rs.getInt("id"); 
                String date = rs.getString("show_date"); 
                String time = rs.getString("show_time"); 
                double pr = rs.getDouble("ticket_price");
                
                JButton slotBtn = new JButton("Date: " + date + " | Slot Time: " + time + " | INR " + pr);
                slotBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
                slotBtn.setBackground(new Color(33, 150, 243)); 
                slotBtn.setForeground(Color.WHITE);
                slotBtn.setFocusPainted(true);
                slotBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
                slotBtn.setMaximumSize(new Dimension(480, 45));
                
                slotBtn.addActionListener(e -> {
                    dial.dispose();
                    TransitionLoader.showTransition(this, "Opening Seat Layout...", () -> {
                        new SeatSelectionFrame(sid, userId, userRole).setVisible(true);
                    });
                });
                dial.add(slotBtn);
                dial.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to retrieve show slots from server: " + ex.getMessage(), "Query Error", JOptionPane.ERROR_MESSAGE);
        }

        if (!hasShows) {
            JLabel lblNoShow = new JLabel("⚠️ No active timing slots allocated for this movie.");
            lblNoShow.setForeground(Color.ORANGE); lblNoShow.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblNoShow.setAlignmentX(Component.CENTER_ALIGNMENT);
            dial.add(lblNoShow);
        }
        dial.setVisible(true);
    }

    private JPanel createProfilePanel() {
        JPanel p = new JPanel(new GridBagLayout()); 
        p.setBackground(new Color(20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints(); 
        gbc.insets = new Insets(15, 15, 15, 15); gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel lblName = new JLabel("Modify Profile System Name Identity:"); 
        lblName.setFont(new Font("SansSerif", Font.BOLD, 14)); lblName.setForeground(Color.WHITE); 
        gbc.gridx = 0; gbc.gridy = 0; p.add(lblName, gbc);
        
        JTextField txtProfName = new JTextField(userName, 18); 
        txtProfName.setFont(new Font("SansSerif", Font.PLAIN, 14)); txtProfName.setPreferredSize(new Dimension(250, 38));
        gbc.gridx = 1; p.add(txtProfName, gbc);
        
        JButton btnUpdate = new JButton("Save Profile Modifications"); 
        btnUpdate.setFont(new Font("SansSerif", Font.BOLD, 14)); btnUpdate.setPreferredSize(new Dimension(300, 42));
        btnUpdate.setBackground(Color.ORANGE); btnUpdate.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; p.add(btnUpdate, gbc);
        
        btnUpdate.addActionListener(e -> {
            String newName = txtProfName.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Profile name input cannot be empty.", "Validation Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (Connection conn = DBConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE users SET name=? WHERE id=?")) {
                ps.setString(1, newName); ps.setInt(2, userId); ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Profile changes saved successfully. Current identity: " + newName, "Execution Complete", JOptionPane.INFORMATION_MESSAGE);
                this.userName = newName;
            } catch (SQLException ex) { 
                JOptionPane.showMessageDialog(this, "Database write failure: " + ex.getMessage(), "Storage Exception", JOptionPane.ERROR_MESSAGE); 
            }
        });
        return p;
    }

    // 🎨 Custom Status Color Renderer for Tables (CONFIRMED = Green, CANCELLED = Red)
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String status = value.toString();
                if (status.equalsIgnoreCase("CONFIRMED")) {
                    c.setForeground(new Color(0, 230, 115)); 
                } else if (status.equalsIgnoreCase("CANCELLED")) {
                    c.setForeground(new Color(240, 84, 84)); 
                } else {
                    c.setForeground(Color.WHITE);
                }
                setFont(new Font("SansSerif", Font.BOLD, 13));
            }
            if (isSelected) {
                c.setBackground(table.getSelectionBackground());
            } else {
                c.setBackground(table.getBackground());
            }
            return c;
        }
    }

    private JPanel createCustomerProfilePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); 
        p.setBackground(new Color(15, 15, 15)); p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        DefaultTableModel model = new DefaultTableModel(new String[]{"Booking ID", "Seats Mapped", "Total Transaction Cost", "Current Status", "Timestamp Execution"}, 0);
        JTable table = new JTable(model); table.setRowHeight(35); table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setBackground(new Color(25, 25, 25)); table.setForeground(Color.WHITE); table.setGridColor(new Color(45, 45, 45));
        
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        
        JTableHeader th = table.getTableHeader(); th.setFont(new Font("SansSerif", Font.BOLD, 14));
        th.setBackground(new Color(35, 35, 35)); th.setForeground(Color.WHITE);
        
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton rf = new JButton("Refresh Audit Records"); rf.setFont(new Font("SansSerif", Font.BOLD, 14)); rf.setPreferredSize(new Dimension(200, 45));
        JButton cn = new JButton("Cancel Selected Booking"); cn.setFont(new Font("SansSerif", Font.BOLD, 14)); cn.setPreferredSize(new Dimension(240, 45));
        cn.setBackground(new Color(240, 84, 84)); cn.setForeground(Color.WHITE); cn.setBorderPainted(false);
        
        rf.addActionListener(e -> {
            model.setRowCount(0);
            try (Connection c = DBConfig.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT id, seats_booked, total_amount, status, booking_time FROM bookings WHERE user_id=?")) {
                ps.setInt(1, userId); ResultSet rs = ps.executeQuery();
                
                DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a");

                while (rs.next()) {
                    String rawTime = rs.getString("booking_time");
                    String formattedTime = rawTime;
                    try {
                        LocalDateTime dt = LocalDateTime.parse(rawTime.substring(0, 19), inputFormat);
                        formattedTime = dt.format(outputFormat);
                    } catch (Exception ignored) {}

                    model.addRow(new Object[]{
                        rs.getInt("id"), 
                        rs.getString("seats_booked"), 
                        "INR " + rs.getDouble("total_amount"), 
                        rs.getString("status"), 
                        formattedTime
                    }); 
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to synchronize logs: " + ex.getMessage(), "Fetch Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cn.addActionListener(e -> {
            int r = table.getSelectedRow(); 
            if (r == -1) { 
                JOptionPane.showMessageDialog(this, "Please choose a booking record from the table list first.", "No Selection", JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            int bid = (int) table.getValueAt(r, 0); 
            String st = (String) table.getValueAt(r, 3); 
            
            if (st != null && st.equalsIgnoreCase("CANCELLED")) { 
                JOptionPane.showMessageDialog(this, "This ticket record has already been cancelled.", "Status Conflict", JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            
            int confirmCancel = JOptionPane.showConfirmDialog(this, "Kya aap waqai yeh booking cancel karna chahte hain?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirmCancel == JOptionPane.YES_OPTION) {
                try {
                    bookingDAO.cancelBooking(bid); 
                    JOptionPane.showMessageDialog(this, "Transaction reversal complete. Ticket cancelled successfully.", "Process Complete", JOptionPane.INFORMATION_MESSAGE); 
                    rf.doClick();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage(), "Execution Failure", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10)); bp.setBackground(new Color(15, 15, 15));
        bp.add(rf); bp.add(cn); p.add(bp, BorderLayout.SOUTH); rf.doClick();
        return p;
    }

    private JPanel createAdminAllBookingsPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); 
        p.setBackground(new Color(15, 15, 15)); 
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchPanel.setBackground(new Color(25, 25, 25));
        searchPanel.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45)));

        JLabel lblSearch = new JLabel("🔍 Find Booking (User/Seats/Status):");
        lblSearch.setForeground(Color.WHITE);
        lblSearch.setFont(new Font("SansSerif", Font.BOLD, 14));

        JTextField txtAdminSearch = new JTextField(25);
        txtAdminSearch.setPreferredSize(new Dimension(300, 38));
        txtAdminSearch.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtAdminSearch.setBackground(new Color(35, 35, 35));
        txtAdminSearch.setForeground(Color.WHITE);
        txtAdminSearch.setCaretColor(Color.WHITE);
        txtAdminSearch.setBorder(new LineBorder(new Color(60, 60, 60), 1));

        searchPanel.add(lblSearch);
        searchPanel.add(txtAdminSearch);
        p.add(searchPanel, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Booking ID", "User ID", "User Name", "Seats Mapped", "Amount (INR)", "Status", "Timestamp"}, 0);
        JTable table = new JTable(model); 
        table.setRowHeight(35); 
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setBackground(new Color(25, 25, 25)); 
        table.setForeground(Color.WHITE); 
        table.setGridColor(new Color(45, 45, 45));
        
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());
        
        JTableHeader th = table.getTableHeader(); 
        th.setFont(new Font("SansSerif", Font.BOLD, 14));
        th.setBackground(new Color(35, 35, 35)); 
        th.setForeground(Color.WHITE);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtAdminSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String text = txtAdminSearch.getText().trim();
                if (text.length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton rf = new JButton("🔄 Refresh All Records"); 
        rf.setFont(new Font("SansSerif", Font.BOLD, 14)); 
        rf.setPreferredSize(new Dimension(220, 45));
        rf.setBackground(new Color(33, 150, 243));
        rf.setForeground(Color.WHITE);

        JButton cn = new JButton("⚠️ Cancel Any Booking (Admin)"); 
        cn.setFont(new Font("SansSerif", Font.BOLD, 14)); 
        cn.setPreferredSize(new Dimension(260, 45));
        cn.setBackground(new Color(240, 84, 84)); 
        cn.setForeground(Color.WHITE); 
        cn.setBorderPainted(false);
        
        rf.addActionListener(e -> {
            model.setRowCount(0);
            String query = "SELECT b.id, b.user_id, u.name as user_name, b.seats_booked, b.total_amount, b.status, b.booking_time " +
                           "FROM bookings b JOIN users u ON b.user_id = u.id ORDER BY b.id DESC";
            try (Connection c = DBConfig.getConnection(); PreparedStatement ps = c.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
                
                DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm:ss a");

                while (rs.next()) {
                    String rawTime = rs.getString("booking_time");
                    String formattedTime = rawTime;
                    try {
                        LocalDateTime dt = LocalDateTime.parse(rawTime.substring(0, 19), inputFormat);
                        formattedTime = dt.format(outputFormat);
                    } catch (Exception ignored) {}

                    model.addRow(new Object[]{
                        rs.getInt("id"), 
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("seats_booked"), 
                        "INR " + rs.getDouble("total_amount"), 
                        rs.getString("status"), 
                        formattedTime
                    }); 
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load audit logs: " + ex.getMessage(), "Fetch Exception", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cn.addActionListener(e -> {
            int r = table.getSelectedRow(); 
            if (r == -1) { 
                JOptionPane.showMessageDialog(this, "Please select a booking record from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            int modelRow = table.convertRowIndexToModel(r);
            int bid = (int) model.getValueAt(modelRow, 0); 
            String st = (String) model.getValueAt(modelRow, 5); 
            
            if (st != null && st.equalsIgnoreCase("CANCELLED")) { 
                JOptionPane.showMessageDialog(this, "This ticket record is already cancelled.", "Status Conflict", JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            
            int confirmCancel = JOptionPane.showConfirmDialog(this, "ADMIN OVERRIDE: Kya aap waqai yeh booking cancel karna chahte hain?", "Confirm Admin Reversal", JOptionPane.YES_NO_OPTION);
            if (confirmCancel == JOptionPane.YES_OPTION) {
                try {
                    bookingDAO.cancelBooking(bid); 
                    JOptionPane.showMessageDialog(this, "Admin override complete. Booking cancelled successfully.", "Process Complete", JOptionPane.INFORMATION_MESSAGE); 
                    rf.doClick();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Cancellation failed: " + ex.getMessage(), "Execution Failure", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10)); 
        bp.setBackground(new Color(15, 15, 15));
        bp.add(rf); 
        bp.add(cn); 
        p.add(bp, BorderLayout.SOUTH); 
        rf.doClick();
        
        return p;
    }

    private JPanel createAdminPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10)); 
        p.setBackground(new Color(20, 20, 20)); p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel lbl = new JLabel("ADMIN SYSTEM PIPELINE & DATA METRICS PORTAL CENTER", SwingConstants.CENTER); 
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15)); lbl.setForeground(Color.YELLOW); 
        p.add(lbl, BorderLayout.NORTH);
        
        DefaultTableModel m = new DefaultTableModel(new String[]{"ID", "Title", "Genre", "Format", "Cert", "Rating", "Poster URL"}, 0);
        JTable t = new JTable(m); t.setRowHeight(35); t.setFont(new Font("SansSerif", Font.PLAIN, 14));
        t.setBackground(new Color(25, 25, 25)); tableForegroundConfiguration(t);
        
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        
        JPanel bulkControlPanel = new JPanel(new BorderLayout(10, 10));
        bulkControlPanel.setBackground(new Color(30, 30, 30));
        bulkControlPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), "Industrial CSV Processing Core Pipeline", 0, 0, null, Color.WHITE));
        
        JProgressBar uploadProgress = new JProgressBar(0, 100);
        uploadProgress.setStringPainted(true); uploadProgress.setFont(new Font("SansSerif", Font.BOLD, 13));
        uploadProgress.setForeground(new Color(0, 200, 115)); uploadProgress.setBackground(Color.DARK_GRAY);
        uploadProgress.setPreferredSize(new Dimension(400, 35));
        
        btnUploadBulk = new JButton("📁 Browse & Import CSV Engine (Ctrl+U)");
        btnUploadBulk.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnUploadBulk.setBackground(new Color(255, 193, 7)); btnUploadBulk.setForeground(Color.BLACK);
        
        JPanel progressWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        progressWrapper.setBackground(new Color(30, 30, 30));
        progressWrapper.add(btnUploadBulk); progressWrapper.add(uploadProgress);
        bulkControlPanel.add(progressWrapper, BorderLayout.CENTER);

        JPanel formContainer = new JPanel(new GridLayout(1, 3, 15, 15));
        formContainer.setBackground(new Color(20, 20, 20));

        JPanel crudForm = new JPanel(new GridLayout(2, 4, 10, 10));
        crudForm.setBackground(new Color(240, 240, 240)); crudForm.setBorder(BorderFactory.createTitledBorder("1. Add Movie Details"));
        JTextField fTitle = new JTextField(); JTextField fGenre = new JTextField(); 
        JTextField fFmt = new JTextField("3D"); JTextField fUrl = new JTextField();
        crudForm.add(new JLabel("Title:")); crudForm.add(fTitle);
        crudForm.add(new JLabel("Genre:")); crudForm.add(fGenre);
        crudForm.add(new JLabel("Format:")); crudForm.add(fFmt);
        crudForm.add(new JLabel("Poster URL:")); crudForm.add(fUrl);
        formContainer.add(crudForm);

        JPanel showForm = new JPanel(new GridLayout(2, 4, 10, 10));
        showForm.setBackground(new Color(225, 235, 245)); showForm.setBorder(BorderFactory.createTitledBorder("2. Allocate Show Slot"));
        comboAdminMovies = new JComboBox<>();
        JTextField fDate = new JTextField("2026-07-25");
        JTextField fTime = new JTextField("18:30:00");
        JTextField fPrice = new JTextField("300.00");
        showForm.add(new JLabel("Movie:")); showForm.add(comboAdminMovies);
        showForm.add(new JLabel("Date (YYYY-MM-DD):")); showForm.add(fDate);
        showForm.add(new JLabel("Time (HH:MM:SS):")); showForm.add(fTime);
        showForm.add(new JLabel("Price (INR):")); showForm.add(fPrice);
        formContainer.add(showForm);
        
        JPanel posterInsertForm = new JPanel(new GridLayout(2, 2, 10, 10));
        posterInsertForm.setBackground(new Color(255, 245, 215)); 
        posterInsertForm.setBorder(BorderFactory.createTitledBorder("3. Manual Poster Identity Linker"));
        comboManualInsertMovies = new JComboBox<>();
        txtManualPosterUrl = new JTextField();
        posterInsertForm.add(new JLabel("Target Movie:")); posterInsertForm.add(comboManualInsertMovies);
        posterInsertForm.add(new JLabel("HTTP Link URL:")); posterInsertForm.add(txtManualPosterUrl);
        formContainer.add(posterInsertForm);

        JButton btnAdd = new JButton("Add Movie"); 
        btnAdd.setFont(new Font("SansSerif", Font.BOLD, 14)); btnAdd.setBackground(Color.GREEN); btnAdd.setForeground(Color.BLACK);
        
        btnAddShow = new JButton("Allocate Show Slot (Ctrl+A)"); 
        btnAddShow.setFont(new Font("SansSerif", Font.BOLD, 14)); btnAddShow.setBackground(new Color(33, 150, 243)); btnAddShow.setForeground(Color.WHITE);

        btnSaveManualPoster = new JButton("🔗 Commit Poster (Ctrl+P)");
        btnSaveManualPoster.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSaveManualPoster.setBackground(new Color(255, 152, 0)); btnSaveManualPoster.setForeground(Color.WHITE);

        btnLoad = new JButton("Synchronize Console (Ctrl+R)"); btnLoad.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnWipeOut = new JButton("🚨 DESTRUCTIVE SYSTEM WIPE (Ctrl+W)"); btnWipeOut.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnWipeOut.setBackground(new Color(180, 0, 0)); btnWipeOut.setForeground(Color.WHITE);

        Runnable reloadMovieDropdown = () -> {
            SwingUtilities.invokeLater(() -> {
                comboAdminMovies.removeAllItems();
                comboManualInsertMovies.removeAllItems();
                try (Connection c = DBConfig.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT id, title FROM movies ORDER BY id DESC")) {
                    while(rs.next()) {
                        String entryItem = rs.getInt("id") + " - " + rs.getString("title");
                        comboAdminMovies.addItem(entryItem);
                        comboManualInsertMovies.addItem(entryItem);
                    }
                } catch(SQLException ignored) {}
            });
        };

        btnLoad.addActionListener(e -> {
            m.setRowCount(0);
            try (Connection c = DBConfig.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM movies")) {
                while (rs.next()) m.addRow(new Object[]{rs.getInt("id"), rs.getString("title"), rs.getString("genre"), rs.getString("format_type"), rs.getString("certification"), rs.getDouble("rating"), rs.getString("poster_url")});
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "System failure during grid synchronization: " + ex.getMessage(), "Sync Error", JOptionPane.ERROR_MESSAGE);
            }
            reloadMovieDropdown.run();
        });

        btnSaveManualPoster.addActionListener(e -> {
            String selectedItem = (String) comboManualInsertMovies.getSelectedItem();
            String targetLink = txtManualPosterUrl.getText().trim();
            if (selectedItem == null || targetLink.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Required fields are missing. Please enter a valid URL.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int targetMovieId = Integer.parseInt(selectedItem.split(" - ")[0]);
            
            try (Connection conn = DBConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE movies SET poster_url=? WHERE id=?")) {
                ps.setString(1, targetLink);
                ps.setInt(2, targetMovieId);
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Asset modification committed successfully.", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
                txtManualPosterUrl.setText(""); 
                btnLoad.doClick(); 
                loadMoviesCatalogueFiltered("", "All Genres", "All Formats");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to update asset storage: " + ex.getMessage(), "SQL Rejection", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnUploadBulk.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Target CSV Storage Sheet");
            javax.swing.filechooser.FileNameExtensionFilter filter = new javax.swing.filechooser.FileNameExtensionFilter("Data Stream Files (*.csv)", "csv");
            fileChooser.setFileFilter(filter); fileChooser.setAcceptAllFileFilterUsed(false); 
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToUpload = fileChooser.getSelectedFile();
                btnUploadBulk.setEnabled(false);
                
                SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                    private boolean errorFound = false;
                    private String errorMsg = "";

                    @Override
                    protected Void doInBackground() {
                        java.util.List<String> lines = new ArrayList<>();
                        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(fileToUpload))) {
                            String line; while ((line = br.readLine()) != null) { lines.add(line); }
                        } catch (Exception ex) {
                            errorFound = true; errorMsg = ex.getMessage();
                            return null;
                        }
                        if (lines.size() <= 1) return null; 
                        int totalRecords = lines.size() - 1;
                        String sqlInsert = "INSERT INTO movies (title, genre, format_type, certification, rating, likes_count, poster_url, duration_mins) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                        
                        try (Connection conn = DBConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                            for (int i = 1; i <= totalRecords; i++) {
                                String[] tokens = lines.get(i).split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                                if (tokens.length >= 8) {
                                    ps.setString(1, tokens[0].replace("\"", "").trim());
                                    ps.setString(2, tokens[1].replace("\"", "").trim());
                                    ps.setString(3, tokens[2].replace("\"", "").trim());
                                    ps.setString(4, tokens[3].replace("\"", "").trim());
                                    ps.setDouble(5, Double.parseDouble(tokens[4].trim()));
                                    ps.setString(6, tokens[5].replace("\"", "").trim());
                                    ps.setString(7, tokens[6].replace("\"", "").trim());
                                    ps.setInt(8, Integer.parseInt(tokens[7].trim()));
                                    ps.executeUpdate();
                                }
                                publish((int) (((double) i / totalRecords) * 100));
                                Thread.sleep(5); 
                            }
                        } catch (Exception ex) {
                            errorFound = true; errorMsg = ex.getMessage();
                        }
                        return null;
                    }

                    @Override
                    protected void process(java.util.List<Integer> chunks) { uploadProgress.setValue(chunks.get(chunks.size() - 1)); }

                    @Override
                    protected void done() {
                        btnUploadBulk.setEnabled(true); uploadProgress.setValue(100);
                        if (errorFound) {
                            JOptionPane.showMessageDialog(DashboardFrame.this, "Pipeline insertion broken: " + errorMsg, "Parsing Failure", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(DashboardFrame.this, "Dataset deployment completed successfully.", "Bulk Sync Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                        btnLoad.doClick();
                        loadMoviesCatalogueFiltered("", "All Genres", "All Formats");
                    }
                };
                worker.execute(); 
            }
        });

        btnAddShow.addActionListener(e -> {
            String selectedItem = (String) comboAdminMovies.getSelectedItem();
            if (selectedItem == null || selectedItem.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selection missing. Please target a movie reference record.", "Execution Blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int selectedMovieId = Integer.parseInt(selectedItem.split(" - ")[0]);
            String showDate = fDate.getText().trim(); String showTime = fTime.getText().trim(); String priceStr = fPrice.getText().trim();
            if (showDate.isEmpty() || showTime.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Parameters constraint warning: Input text fields cannot be left blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (Connection c = DBConfig.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO shows (movie_id, show_date, show_time, ticket_price) VALUES (?, ?, ?, ?)")) {
                ps.setInt(1, selectedMovieId); ps.setString(2, showDate); ps.setString(3, showTime); ps.setDouble(4, Double.parseDouble(priceStr));
                ps.executeUpdate(); 
                JOptionPane.showMessageDialog(this, "Operational Slot Allocation Successful.", "Record Committed", JOptionPane.INFORMATION_MESSAGE);
                btnLoad.doClick();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Allocation script rejected: " + ex.getMessage(), "Constraint Breach", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnWipeOut.addActionListener(e -> {
            int firstWarning = JOptionPane.showConfirmDialog(this, "CRITICAL CONSOLE WARNING: This action will permanently erase all movie records and transactions. Do you wish to continue?", "Destructive Deletion Request", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (firstWarning == JOptionPane.YES_OPTION) {
                JPasswordField pf = new JPasswordField();
                int okState = JOptionPane.showConfirmDialog(this, pf, "Identity Verification Required - Enter Admin Password:", JOptionPane.OK_CANCEL_OPTION, JComponent.WHEN_IN_FOCUSED_WINDOW);
                if (okState == JOptionPane.OK_OPTION) {
                    String encryptedHashedInput = SecurityUtil.hashPassword(new String(pf.getPassword()));
                    boolean auth = false;
                    try (Connection conn = DBConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE id=? AND password_hash=? AND role='ADMIN'")) {
                        ps.setInt(1, userId); ps.setString(2, encryptedHashedInput);
                        ResultSet rs = ps.executeQuery(); if (rs.next()) auth = true;
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(this, "Security layer connection timeout: " + ex.getMessage(), "Authentication Framework Failure", JOptionPane.ERROR_MESSAGE);
                    }
                    
                    if (auth) {
                        try (Connection conn = DBConfig.getConnection()) {
                            conn.setAutoCommit(false);
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("DELETE FROM seats"); stmt.executeUpdate("DELETE FROM bookings");
                                stmt.executeUpdate("DELETE FROM shows"); stmt.executeUpdate("DELETE FROM movies");
                                conn.commit(); 
                                JOptionPane.showMessageDialog(this, "Database storage entities wiped clean successfully.", "Destruction Complete", JOptionPane.INFORMATION_MESSAGE);
                                btnLoad.doClick(); loadMoviesCatalogueFiltered("", "All Genres", "All Formats");
                            } catch (SQLException qe) { 
                                conn.rollback(); 
                                JOptionPane.showMessageDialog(this, "SQL execution blocked inside safe transition transaction: " + qe.getMessage(), "Safety Rollback Activated", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(this, "Database infrastructure failure: " + ex.getMessage(), "Connection Interrupted", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Security Authentication Rejection: Incorrect verification password parameters.", "Access Denied", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        btnAdd.addActionListener(e -> {
            if (fTitle.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title text string field cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try (Connection c = DBConfig.getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO movies (title, genre, format_type, certification, rating, likes_count, poster_url, duration_mins) VALUES (?, ?, ?, 'UA', 8.5, '20K', ?, 135)")) {
                ps.setString(1, fTitle.getText().trim()); ps.setString(2, fGenre.getText().trim()); ps.setString(3, fFmt.getText().trim()); ps.setString(4, fUrl.getText().trim());
                ps.executeUpdate(); 
                JOptionPane.showMessageDialog(this, "Single record committed to storage matrix.", "Movie Created", JOptionPane.INFORMATION_MESSAGE); 
                btnLoad.doClick(); loadMoviesCatalogueFiltered("", "All Genres", "All Formats");
                fTitle.setText(""); fGenre.setText(""); fUrl.setText("");
            } catch (SQLException ex) { 
                JOptionPane.showMessageDialog(this, "Manual entry failed: " + ex.getMessage(), "Storage Rejection", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JPanel southMasterContainer = new JPanel(new BorderLayout());
        southMasterContainer.setBackground(new Color(20, 20, 20)); 
        southMasterContainer.add(formConnectionContainer(), BorderLayout.CENTER);
        
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); 
        bp.setBackground(new Color(20, 20, 20));
        bp.add(btnAdd); 
        bp.add(btnAddShow); 
        bp.add(btnSaveManualPoster); 
        bp.add(btnLoad); 
        bp.add(btnWipeOut);
        
        southMasterContainer.add(bp, BorderLayout.SOUTH);
        p.add(bulkControlPanel, BorderLayout.NORTH); 
        p.add(southMasterContainer, BorderLayout.SOUTH);
        
        btnLoad.doClick();
        return p;
    }

    private JPanel formConnectionContainer() {
        JPanel formContainer = new JPanel(new GridLayout(1, 3, 15, 15));
        formContainer.setBackground(new Color(20, 20, 20));

        JPanel crudForm = new JPanel(new GridLayout(2, 4, 10, 10));
        crudForm.setBackground(new Color(240, 240, 240)); crudForm.setBorder(BorderFactory.createTitledBorder("1. Add Movie Details"));
        JTextField fTitle = new JTextField(); JTextField fGenre = new JTextField(); 
        JTextField fFmt = new JTextField("3D"); JTextField fUrl = new JTextField();
        crudForm.add(new JLabel("Title:")); crudForm.add(fTitle);
        crudForm.add(new JLabel("Genre:")); crudForm.add(fGenre);
        crudForm.add(new JLabel("Format:")); crudForm.add(fFmt);
        crudForm.add(new JLabel("Poster URL:")); crudForm.add(fUrl);
        formContainer.add(crudForm);

        JPanel showForm = new JPanel(new GridLayout(2, 4, 10, 10));
        showForm.setBackground(new Color(225, 235, 245)); showForm.setBorder(BorderFactory.createTitledBorder("2. Allocate Show Slot"));
        comboAdminMovies = new JComboBox<>();
        JTextField fDate = new JTextField("2026-07-25");
        JTextField fTime = new JTextField("18:30:00");
        JTextField fPrice = new JTextField("300.00");
        showForm.add(new JLabel("Movie:")); showForm.add(comboAdminMovies);
        showForm.add(new JLabel("Date (YYYY-MM-DD):")); showForm.add(fDate);
        showForm.add(new JLabel("Time (HH:MM:SS):")); showForm.add(fTime);
        showForm.add(new JLabel("Price (INR):")); showForm.add(fPrice);
        formContainer.add(showForm);
        
        JPanel posterInsertForm = new JPanel(new GridLayout(2, 2, 10, 10));
        posterInsertForm.setBackground(new Color(255, 245, 215)); 
        posterInsertForm.setBorder(BorderFactory.createTitledBorder("3. Manual Poster Identity Linker"));
        comboManualInsertMovies = new JComboBox<>();
        txtManualPosterUrl = new JTextField();
        posterInsertForm.add(new JLabel("Target Movie:")); posterInsertForm.add(comboManualInsertMovies);
        posterInsertForm.add(new JLabel("HTTP Link URL:")); posterInsertForm.add(txtManualPosterUrl);
        formContainer.add(posterInsertForm);
        
        return formContainer;
    }

    private void tableForegroundConfiguration(JTable tableInstance) {
        tableInstance.setForeground(Color.WHITE); 
        tableInstance.setGridColor(new Color(50, 50, 50));
        JTableHeader th = tableInstance.getTableHeader(); 
        th.setFont(new Font("SansSerif", Font.BOLD, 14));
        th.setBackground(new Color(40, 40, 40)); 
        th.setForeground(Color.WHITE);
    }
}