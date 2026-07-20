import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProjectBuilder {
    public static void main(String[] args) {
        String srcPath = "src" + File.separator;
        
        writeFile(srcPath + "com/cinegrid/dao/BookingDAO.java", getAdvanceBookingDAOCode());
        writeFile(srcPath + "com/cinegrid/view/AuthFrame.java", getUpdatedAuthFrameCode());
        writeFile(srcPath + "com/cinegrid/view/DashboardFrame.java", getDashboardFrameCode());
        writeFile(srcPath + "com/cinegrid/MainApp.java", getUpdatedMainAppCode());

        System.out.println("\n[SUCCESS]: System highly optimized and structuralized to Advanced BookMyShow UI scale!");
        System.out.println("[Action]: Ab aap 'MainApp.java' ko directly Run kar sakte hain.");
    }

    private static void writeFile(String path, String content) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(content);
            System.out.println("[File Updated]: " + path);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String getAdvanceBookingDAOCode() {
        return "package com.cinegrid.dao;\n\nimport com.cinegrid.config.DBConfig;\nimport com.cinegrid.model.Seat;\nimport java.sql.*;\nimport java.util.*;\n\npublic class BookingDAO {\n" +
               "    public boolean bookSelectedSeats(Set<Integer> seatIds, int userId, int showId, double totalAmount, String seatLabels) throws SQLException {\n" +
               "        Connection conn = null; PreparedStatement bkStmt = null; PreparedStatement updStmt = null;\n" +
               "        try {\n" +
               "            conn = DBConfig.getConnection(); conn.setAutoCommit(false);\n" +
               "            String bkSql = \"INSERT INTO bookings (user_id, show_id, seats_booked, total_amount, status) VALUES (?, ?, ?, ?, 'CONFIRMED')\";\n" +
               "            bkStmt = conn.prepareStatement(bkSql, Statement.RETURN_GENERATED_KEYS);\n" +
               "            bkStmt.setInt(1, userId); bkStmt.setInt(2, showId); bkStmt.setString(3, seatLabels); bkStmt.setDouble(4, totalAmount);\n" +
               "            bkStmt.executeUpdate();\n" +
               "            ResultSet rs = bkStmt.getGeneratedKeys(); int bookingId = 0; if(rs.next()) bookingId = rs.getInt(1);\n" +
               "            String updSql = \"UPDATE seats SET is_booked = true, booked_by_user_id = ?, booking_id = ? WHERE id = ?\";\n" +
               "            updStmt = conn.prepareStatement(updSql);\n" +
               "            for(Integer sid : seatIds) {\n" +
               "                updStmt.setInt(1, userId); updStmt.setInt(2, bookingId); updStmt.setInt(3, sid); updStmt.addBatch();\n" +
               "            }\n" +
               "            updStmt.executeBatch(); conn.commit(); return true;\n" +
               "        } catch(Exception e) { if(conn != null) conn.rollback(); throw e; }\n" +
               "        finally { if(conn != null) conn.close(); }\n" +
               "    }\n" +
               "    public void cancelBooking(int bookingId) throws SQLException {\n" +
               "        try (Connection conn = DBConfig.getConnection()) {\n" +
               "            conn.setAutoCommit(false);\n" +
               "            String sql1 = \"UPDATE bookings SET status = 'CANCELLED' WHERE id = ?\";\n" +
               "            String sql2 = \"UPDATE seats SET is_booked = false, booked_by_user_id = NULL, booking_id = NULL WHERE booking_id = ?\";\n" +
               "            try(PreparedStatement p1 = conn.prepareStatement(sql1);\n" +
               "                PreparedStatement p2 = conn.prepareStatement(sql2)) {\n" +
               "                p1.setInt(1, bookingId); p1.executeUpdate();\n" +
               "                p2.setInt(1, bookingId); p2.executeUpdate();\n" +
               "                conn.commit();\n" +
               "            } catch(Exception e) { conn.rollback(); throw e; }\n" +
               "        }\n" +
               "    }\n" +
               "    public void generateSeatsForShow(int showId) throws SQLException {\n" +
               "        String chk = \"SELECT COUNT(*) FROM seats WHERE show_id = ?\";\n" +
               "        try (Connection conn = DBConfig.getConnection(); PreparedStatement cp = conn.prepareStatement(chk)) {\n" +
               "            cp.setInt(1, showId); ResultSet rs = cp.executeQuery();\n" +
               "            if(rs.next() && rs.getInt(1) > 0) return;\n" +
               "        }\n" +
               "        String sql = \"INSERT INTO seats (show_id, row_label, seat_number, is_booked) VALUES (?, ?, ?, false)\";\n" +
               "        try (Connection conn = DBConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {\n" +
               "            conn.setAutoCommit(false);\n" +
               "            for (char row = 'A'; row <= 'J'; row++) {\n" +
               "                for (int num = 1; num <= 10; num++) {\n" +
               "                    pstmt.setInt(1, showId); pstmt.setString(2, String.valueOf(row)); pstmt.setInt(3, num); pstmt.addBatch();\n" +
               "                }\n" +
               "            }\n" +
               "            pstmt.executeBatch(); conn.commit();\n" +
               "        }\n" +
               "    }\n" +
               "    public List<Seat> getSeatsForShow(int showId) throws SQLException {\n" +
               "        List<Seat> seats = new ArrayList<>();\n" +
               "        String sql = \"SELECT * FROM seats WHERE show_id = ? ORDER BY row_label, seat_number\";\n" +
               "        try (Connection conn = DBConfig.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {\n" +
               "            pstmt.setInt(1, showId);\n" +
               "            ResultSet rs = pstmt.executeQuery();\n" +
               "            while (rs.next()) {\n" +
               "                seats.add(new Seat(rs.getInt(\"id\"), rs.getInt(\"show_id\"), rs.getString(\"row_label\").charAt(0), rs.getInt(\"seat_number\"), rs.getBoolean(\"is_booked\")));\n" +
               "            }\n" +
               "        }\n" +
               "        return seats;\n" +
               "    }\n" +
               "}";
    }

    private static String getUpdatedAuthFrameCode() {
        return "package com.cinegrid.view;\n\nimport com.cinegrid.config.DBConfig;\nimport com.cinegrid.util.SecurityUtil;\nimport javax.swing.*;\nimport java.awt.*;\nimport java.sql.*;\n\npublic class AuthFrame extends JFrame {\n    private JTextField txtEmail; private JPasswordField txtPassword;\n    public AuthFrame() {\n        setTitle(\"CineGrid Secure Entry\"); setSize(450, 300); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n        setLocationRelativeTo(null); getContentPane().setBackground(new Color(34, 40, 49)); setLayout(new GridBagLayout());\n        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10,10,10,10); gbc.fill = GridBagConstraints.HORIZONTAL;\n        JLabel title = new JLabel(\"CINEGRID AUTHENTICATION\", SwingConstants.CENTER); title.setFont(new Font(\"Arial\", Font.BOLD, 18)); title.setForeground(new Color(238, 238, 238));\n        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; add(title, gbc); gbc.gridwidth=1;\n        JLabel lblE = new JLabel(\"Email:\"); lblE.setForeground(Color.LIGHT_GRAY); gbc.gridx=0; gbc.gridy=1; add(lblE, gbc);\n        txtEmail = new JTextField(20); gbc.gridx=1; add(txtEmail, gbc);\n        JLabel lblP = new JLabel(\"Password:\"); lblP.setForeground(Color.LIGHT_GRAY); gbc.gridx=0; gbc.gridy=2; add(lblP, gbc);\n        txtPassword = new JPasswordField(20); gbc.gridx=1; add(txtPassword, gbc);\n        JButton btn = new JButton(\"Login System\"); btn.setBackground(new Color(240, 84, 84)); btn.setForeground(Color.WHITE); btn.setFont(new Font(\"Arial\", Font.BOLD, 14));\n        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; add(btn, gbc);\n        btn.addActionListener(e -> {\n            String em = txtEmail.getText().trim(); String pass = new String(txtPassword.getPassword());\n            String hs = SecurityUtil.hashPassword(pass);\n            try(Connection c = DBConfig.getConnection(); PreparedStatement p = c.prepareStatement(\"SELECT id, name, role FROM users WHERE email=? AND password_hash=?\")) {\n                p.setString(1, em); p.setString(2, hs);\n                ResultSet rs = p.executeQuery();\n                if(rs.next()) {\n                    int uid = rs.getInt(\"id\"); String name = rs.getString(\"name\"); String role = rs.getString(\"role\");\n                    JOptionPane.showMessageDialog(this, \"Welcome \" + name + \" (\" + role + \")\");\n                    new DashboardFrame(uid, name, role).setVisible(true); this.dispose();\n                } else { JOptionPane.showMessageDialog(this, \"Invalid email/password matrix!\"); }\n            } catch(Exception ex) { JOptionPane.showMessageDialog(this, \"Error: \"+ex.getMessage()); }\n        });\n    }\n}";
    }

    private static String getDashboardFrameCode() {
        return "package com.cinegrid.view;\n\nimport com.cinegrid.config.DBConfig;\nimport com.cinegrid.dao.BookingDAO;\nimport javax.swing.*;\nimport javax.swing.border.LineBorder;\nimport javax.swing.table.DefaultTableModel;\nimport java.awt.*;\nimport java.sql.*;\nimport java.time.LocalDateTime;\nimport java.time.format.DateTimeFormatter;\nimport java.util.Vector;\n\npublic class DashboardFrame extends JFrame {\n    private int userId; private String userName; private String userRole;\n    private JPanel mainContentPanel; private JTabbedPane tabbedPane;\n    private BookingDAO bookingDAO = new BookingDAO();\n\n    public DashboardFrame(int userId, String userName, String userRole) {\n        this.userId = userId; this.userName = userName; this.userRole = userRole;\n        setTitle(\"CineGrid Dashboard - \" + userName + \" (\" + userRole + \")\");\n        setSize(1200, 800); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setLocationRelativeTo(null);\n        getContentPane().setBackground(new Color(18, 18, 18));\n        \n        JPanel header = new JPanel(new BorderLayout()); header.setBackground(new Color(33, 33, 33)); header.setPreferredSize(new Dimension(1200, 70));\n        JLabel logo = new JLabel(\"  CineGrid\", SwingConstants.LEFT); logo.setFont(new Font(\"Arial\", Font.BOLD, 24)); logo.setForeground(new Color(248, 68, 100));\n        JLabel info = new JLabel(\"User: \" + userName + \"  |  Role: \" + userRole + \"    \", SwingConstants.RIGHT); info.setForeground(Color.WHITE); info.setFont(new Font(\"Arial\", Font.PLAIN, 14));\n        header.add(logo, BorderLayout.WEST); header.add(info, BorderLayout.EAST); add(header, BorderLayout.NORTH);\n\n        tabbedPane = new JTabbedPane();\n        tabbedPane.setBackground(new Color(33, 33, 33)); tabbedPane.setForeground(Color.WHITE);\n        \n        JPanel moviePanel = new JPanel(new BorderLayout()); moviePanel.setBackground(new Color(18, 18, 18));\n        mainContentPanel = new JPanel(new GridLayout(0, 4, 20, 20)); mainContentPanel.setBackground(new Color(18, 18, 18));\n        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));\n        JScrollPane scroll = new JScrollPane(mainContentPanel); scroll.setBorder(null); moviePanel.add(scroll, BorderLayout.CENTER);\n        tabbedPane.addTab(\"Recommended Movies\", moviePanel);\n\n        if(userRole.equalsIgnoreCase(\"CUSTOMER\")) {\n            tabbedPane.addTab(\"My Purchase History\", createCustomerProfilePanel());\n        } else {\n            tabbedPane.addTab(\"Admin Control Panel Center\", createAdminPanel());\n        }\n\n        add(tabbedPane, BorderLayout.CENTER);\n        loadMoviesCatalogue();\n    }\n\n    private void loadMoviesCatalogue() {\n        mainContentPanel.removeAll();\n        try (Connection conn = DBConfig.getConnection();\n             Statement stmt = conn.createStatement();\n             ResultSet rs = stmt.executeQuery(\"SELECT * FROM movies\")) {\n            while(rs.next()) {\n                int mid = rs.getInt(\"id\"); String title = rs.getString(\"title\"); String genre = rs.getString(\"genre\");\n                String fmt = rs.getString(\"format_type\"); String cert = rs.getString(\"certification\");\n                double rat = rs.getDouble(\"rating\"); String likes = rs.getString(\"likes_count\");\n                \n                JPanel card = new JPanel(); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));\n                card.setBackground(new Color(28, 28, 28)); card.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));\n                \n                JLabel posterLabel = new JLabel(\"POSTER MOCKUP\", SwingConstants.CENTER);\n                posterLabel.setPreferredSize(new Dimension(220, 280)); posterLabel.setMaximumSize(new Dimension(220, 280));\n                posterLabel.setBackground(Color.GRAY); posterLabel.setOpaque(true); posterLabel.setForeground(Color.WHITE);\n                posterLabel.setAlignmentX(Component.CENTER_ALIGNMENT);\n                \n                JLabel lblLike = new JLabel(\"  \" + rat + \"/10  \" + likes + \" Votes\"); lblLike.setForeground(Color.LIGHT_GRAY); lblLike.setAlignmentX(Component.CENTER_ALIGNMENT);\n                JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font(\"Arial\", Font.BOLD, 16)); lblTitle.setForeground(Color.WHITE); lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);\n                JLabel lblMeta = new JLabel(fmt + \" | \" + cert + \" | \" + genre); lblMeta.setForeground(Color.GRAY); lblMeta.setAlignmentX(Component.CENTER_ALIGNMENT);\n                \n                JButton btnBook = new JButton(\"Book Tickets\"); btnBook.setBackground(new Color(248, 68, 100)); btnBook.setForeground(Color.WHITE); btnBook.setAlignmentX(Component.CENTER_ALIGNMENT);\n                btnBook.addActionListener(e -> openScheduleSelector(mid, title));\n\n                card.add(Box.createRigidArea(new Dimension(0, 10))); card.add(posterLabel);\n                card.add(Box.createRigidArea(new Dimension(0, 5))); card.add(lblLike);\n                card.add(Box.createRigidArea(new Dimension(0, 5))); card.add(lblTitle);\n                card.add(Box.createRigidArea(new Dimension(0, 5))); card.add(lblMeta);\n                card.add(Box.createRigidArea(new Dimension(0, 10))); card.add(btnBook);\n                card.add(Box.createRigidArea(new Dimension(0, 10)));\n                \n                mainContentPanel.add(card);\n            }\n        } catch(Exception ex) { ex.printStackTrace(); }\n        mainContentPanel.revalidate(); mainContentPanel.repaint();\n    }\n\n    private void openScheduleSelector(int movieId, String title) {\n        JDialog dial = new JDialog(this, \"Select Slot for \" + title, true);\n        dial.setSize(500, 350); dial.setLocationRelativeTo(this); dial.setLayout(new GridLayout(0, 1, 10, 10));\n        dial.getContentPane().setBackground(new Color(28, 28, 28));\n        JLabel head = new JLabel(\"Available Slots:\", SwingConstants.CENTER); head.setForeground(Color.WHITE); dial.add(head);\n        try (Connection conn = DBConfig.getConnection();\n             PreparedStatement ps = conn.prepareStatement(\"SELECT id, show_date, show_time, ticket_price FROM shows WHERE movie_id=?\")) {\n            ps.setInt(1, movieId); ResultSet rs = ps.executeQuery();\n            while(rs.next()) {\n                int sid = rs.getInt(\"id\"); String date = rs.getString(\"show_date\"); String time = rs.getString(\"show_time\");\n                double pr = rs.getDouble(\"ticket_price\");\n                JButton slotBtn = new JButton(date + \" | Time Slot: \" + time + \" | Price: INR \" + pr);\n                slotBtn.setBackground(new Color(33, 150, 243)); slotBtn.setForeground(Color.WHITE);\n                slotBtn.addActionListener(e -> {\n                    dial.dispose();\n                    new SeatSelectionFrame(sid, userId).setVisible(true);\n                });\n                dial.add(slotBtn);\n            }\n        } catch(Exception ex) { ex.printStackTrace(); }\n        dial.setVisible(true);\n    }\n\n    private JPanel createCustomerProfilePanel() {\n        JPanel p = new JPanel(new BorderLayout()); p.setBackground(new Color(18,18,18));\n        DefaultTableModel model = new DefaultTableModel(new String[]{\"Booking ID\", \"Seats\", \"Total Cost\", \"Status\", \"Booking Time\"}, 0);\n        JTable table = new JTable(model); JScrollPane sp = new JScrollPane(table); p.add(sp, BorderLayout.CENTER);\n        JButton refreshBtn = new JButton(\"Refresh Purchase Records & History\");\n        JButton cancelBtn = new JButton(\"Cancel Selected Booking (Allowed before 2 Hours)\");\n        cancelBtn.setBackground(Color.RED); cancelBtn.setForeground(Color.WHITE);\n        refreshBtn.addActionListener(e -> {\n            model.setRowCount(0);\n            try(Connection c = DBConfig.getConnection(); PreparedStatement ps = c.prepareStatement(\"SELECT id, seats_booked, total_amount, status, booking_time FROM bookings WHERE user_id=?\")) {\n                ps.setInt(1, userId); ResultSet rs = ps.executeQuery();\n                while(rs.next()) model.addRow(new Object[]{rs.getInt(\"id\"), rs.getString(\"seats_booked\"), rs.getDouble(\"total_amount\"), rs.getString(\"status\"), rs.getString(\"booking_time\")});\n            } catch(Exception ex) { ex.printStackTrace(); }\n        });\n        cancelBtn.addActionListener(e -> {\n            int row = table.getSelectedRow(); if(row == -1) return;\n            int bid = (int) table.getValueAt(row, 0); String status = (String) table.getValueAt(row, 3);\n            String bTimeStr = (String) table.getValueAt(row, 4);\n            if(status.equals(\"CANCELLED\")) { JOptionPane.showMessageDialog(this, \"Already Cancelled!\"); return; }\n            try {\n                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\");\n                LocalDateTime bookingTime = LocalDateTime.parse(bTimeStr.substring(0, 19), formatter);\n                if(LocalDateTime.now().isAfter(bookingTime.plusHours(2))) {\n                    JOptionPane.showMessageDialog(this, \"Error: Cancel window threshold crossed (Max 2 hours).\");\n                    return;\n                }\n                bookingDAO.cancelBooking(bid); JOptionPane.showMessageDialog(this, \"Booking Cancelled Successfully!\"); refreshBtn.doClick();\n            } catch(Exception ex) { JOptionPane.showMessageDialog(this, \"Error: \"+ex.getMessage()); }\n        });\n        JPanel bp = new JPanel(); bp.add(refreshBtn); bp.add(cancelBtn); p.add(bp, BorderLayout.SOUTH);\n        return p;\n    }\n\n    private JPanel createAdminPanel() {\n        JPanel p = new JPanel(new BorderLayout()); p.setBackground(new Color(28,28,28));\n        JLabel lbl = new JLabel(\"ADMIN MASTER CONTROL CENTER\", SwingConstants.CENTER); lbl.setForeground(Color.YELLOW); p.add(lbl, BorderLayout.NORTH);\n        DefaultTableModel m = new DefaultTableModel(new String[]{\"User ID\", \"Name\", \"Email\", \"Role\"}, 0);\n        JTable t = new JTable(m); p.add(new JScrollPane(t), BorderLayout.CENTER);\n        JButton loadUsers = new JButton(\"Load System Database Users\");\n        loadUsers.addActionListener(e -> {\n            m.setRowCount(0);\n            try(Connection c = DBConfig.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(\"SELECT id, name, email, role FROM users\")) {\n                while(rs.next()) m.addRow(new Object[]{rs.getInt(\"id\"), rs.getString(\"name\"), rs.getString(\"email\"), rs.getString(\"role\")});\n            } catch(Exception ex) {}\n        });\n        p.add(loadUsers, BorderLayout.SOUTH);\n        return p;\n    }\n}";
    }

    private static String getUpdatedMainAppCode() {
        return "package com.cinegrid;\n\nimport com.cinegrid.view.AuthFrame;\nimport javax.swing.SwingUtilities;\n\npublic class MainApp {\n    public static void main(String[] args) {\n        SwingUtilities.invokeLater(() -> { new AuthFrame().setVisible(true); });\n    }\n}";
    }
}