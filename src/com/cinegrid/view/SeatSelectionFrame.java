package com.cinegrid.view;

import com.cinegrid.dao.BookingDAO;
import com.cinegrid.model.Seat;
import com.cinegrid.util.ReceiptPrinterUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.QuadCurve2D;
import java.sql.SQLException;
import java.util.*;

public class SeatSelectionFrame extends JFrame {
    private int currentShowId; 
    private int currentUserId; 
    private String userRole; // Role tracking field for Admin/Customer split
    private BookingDAO bookingDAO = new BookingDAO();
    private Set<Integer> selectedSeatIds = new HashSet<>(); 
    private Map<Integer, String> selectedSeatLabels = new HashMap<>(); 
    private JPanel mainLayoutContainer; 
    private JButton btnConfirm;
    private JLabel lblTotalSummary;
    
    private Map<Integer, Double> seatPriceMap = new HashMap<>();
    private double currentTotalAmount = 0.0;

    public SeatSelectionFrame(int showId, int userId, String userRole) {
        this.currentShowId = showId; 
        this.currentUserId = userId;
        this.userRole = userRole;
        
        setTitle("CineGrid Audi Seating - Live Tier Layout"); 
        
        // Forces the Seat Selection window to open directly in FULL SCREEN Maximized state
        setExtendedState(JFrame.MAXIMIZED_BOTH); 
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        getRootPane().getActionMap().put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { dispose(); }
        });

        // Top Content: Color Legends Bar
        JPanel topHeaderContainer = new JPanel(new BorderLayout());
        topHeaderContainer.setBackground(new Color(22, 22, 22));
        
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 15));
        legendPanel.setBackground(new Color(28, 28, 28));
        legendPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(45, 45, 45)));
        legendPanel.add(createLegendItem(" Available", Color.WHITE, new Color(74, 194, 120)));
        legendPanel.add(createLegendItem(" Selected", new Color(74, 194, 120), new Color(50, 160, 95)));
        legendPanel.add(createLegendItem(" Sold Out", new Color(45, 45, 45), new Color(60, 60, 60)));
        topHeaderContainer.add(legendPanel, BorderLayout.CENTER);
        add(topHeaderContainer, BorderLayout.NORTH);

        // Main seating layout area
        mainLayoutContainer = new JPanel();
        mainLayoutContainer.setLayout(new BoxLayout(mainLayoutContainer, BoxLayout.Y_AXIS));
        mainLayoutContainer.setBackground(new Color(22, 22, 22));
        mainLayoutContainer.setBorder(new EmptyBorder(10, 40, 20, 40));
        
        JScrollPane verticalContainerScroll = new JScrollPane(mainLayoutContainer);
        verticalContainerScroll.setBorder(null);
        verticalContainerScroll.getVerticalScrollBar().setUnitIncrement(25);
        add(verticalContainerScroll, BorderLayout.CENTER);

        // --- BOTTOM PANEL: CONTAINS FIXED NON-OVERLAPPING SCREEN ---
        JPanel bottomMasterPanel = new JPanel();
        bottomMasterPanel.setLayout(new BoxLayout(bottomMasterPanel, BoxLayout.Y_AXIS));
        bottomMasterPanel.setBackground(new Color(22, 22, 22));

        // 1. DYNAMIC SCREEN BACKDROP COMPONENT (FIXED GAPS OVERLAP)
        JPanel cinemaScreenComponent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Silver soft reflection light drop gradient
                GradientPaint screenIllumination = new GradientPaint(0, getHeight(), new Color(180, 210, 255, 35), 0, 0, new Color(22, 22, 22, 0));
                g2.setPaint(screenIllumination);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Screen Arc Vector Curve representation
                g2.setColor(new Color(200, 220, 255, 180));
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Lowered down anchor points to protect overlapping bounds
                QuadCurve2D curvedScreenLine = new QuadCurve2D.Float(250, 40, getWidth() / 2, 60, getWidth() - 250, 40);
                g2.draw(curvedScreenLine);
            }
        };
        cinemaScreenComponent.setPreferredSize(new Dimension(1250, 95));
        cinemaScreenComponent.setBackground(new Color(22, 22, 22));
        cinemaScreenComponent.setLayout(new BorderLayout());
        cinemaScreenComponent.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));
        
        JLabel lblScreenLabel = new JLabel("SCREEN THIS WAY", SwingConstants.CENTER);
        lblScreenLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblScreenLabel.setForeground(Color.GRAY);
        cinemaScreenComponent.add(lblScreenLabel, BorderLayout.NORTH); // Text shifted to North bounds to avoid line overlap
        bottomMasterPanel.add(cinemaScreenComponent);

        // 2. Transaction Summary Checkout Indicator bar
        JPanel bottomActionPanel = new JPanel(new BorderLayout(20, 0));
        bottomActionPanel.setBackground(new Color(30, 30, 30));
        bottomActionPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        bottomActionPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(45, 45, 45)));
        
        lblTotalSummary = new JLabel("Seats Selected: 0  |  Total Amount: INR 0.00", SwingConstants.LEFT);
        lblTotalSummary.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalSummary.setForeground(Color.WHITE);
        bottomActionPanel.add(lblTotalSummary, BorderLayout.WEST);
        
        btnConfirm = new JButton("Confirm Movie Tickets"); 
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnConfirm.setBackground(new Color(248, 68, 100)); 
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(280, 50));
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBorder(new LineBorder(new Color(248, 68, 100), 1, true));
        btnConfirm.addActionListener(e -> processCheckout()); 
        bottomActionPanel.add(btnConfirm, BorderLayout.EAST);
        
        bottomMasterPanel.add(bottomActionPanel);
        add(bottomMasterPanel, BorderLayout.SOUTH);
        
        loadSeatMatrixEngine(); 
    }

    private JPanel createLegendItem(String text, Color bg, Color border) {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        container.setBackground(new Color(28, 28, 28));
        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(border); g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
            }
        };
        icon.setPreferredSize(new Dimension(16, 18));
        icon.setBackground(new Color(28, 28, 28));
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13)); label.setForeground(Color.LIGHT_GRAY);
        container.add(icon); container.add(label);
        return container;
    }

    private void loadSeatMatrixEngine() {
        mainLayoutContainer.removeAll();
        try {
            java.util.List<Seat> seats = bookingDAO.getSeatsForShow(currentShowId);
            if (seats.isEmpty()) {
                bookingDAO.generateSeatsForShow(currentShowId); 
                seats = bookingDAO.getSeatsForShow(currentShowId); 
            }
            
            Map<String, java.util.List<Seat>> groupedRows = new TreeMap<>(Collections.reverseOrder());
            for (Seat s : seats) {
                String row = String.valueOf(s.getRowLabel()).trim().toUpperCase();
                groupedRows.computeIfAbsent(row, k -> new ArrayList<>()).add(s);
            }

            renderCategoryBlock("₹270 PREMIUM XL", groupedRows, new String[]{"M"}, 270.0);
            renderCategoryBlock("₹230 PREMIUM", groupedRows, new String[]{"L", "K", "J", "H"}, 230.0);
            renderCategoryBlock("₹210 EXECUTIVE", groupedRows, new String[]{"G", "F", "E"}, 210.0);
            renderCategoryBlock("₹200 NORMAL", groupedRows, new String[]{"D"}, 200.0);

        } catch (SQLException e) { 
            JOptionPane.showMessageDialog(this, "Failed to build seat structure matrix: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); 
        }
        mainLayoutContainer.revalidate();
        mainLayoutContainer.repaint();
    }

    private void renderCategoryBlock(String categoryTitle, Map<String, java.util.List<Seat>> groupedRows, String[] targetedRows, double categoryPrice) {
        boolean blockHasData = false;
        for (String rName : targetedRows) {
            if (groupedRows.containsKey(rName)) blockHasData = true;
        }
        if (!blockHasData) return;

        JLabel lblHeader = new JLabel(categoryTitle);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblHeader.setForeground(new Color(160, 170, 180));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 8, 0));
        mainLayoutContainer.add(lblHeader);

        JPanel separatorLine = new JPanel();
        separatorLine.setMaximumSize(new Dimension(1000, 1));
        separatorLine.setBackground(new Color(50, 50, 50));
        mainLayoutContainer.add(separatorLine);
        mainLayoutContainer.add(Box.createRigidArea(new Dimension(0, 12)));

        for (String rowName : targetedRows) {
            if (!groupedRows.containsKey(rowName)) continue;
            
            java.util.List<Seat> rowSeats = groupedRows.get(rowName);
            rowSeats.sort((s1, s2) -> Integer.compare(s2.getSeatNumber(), s1.getSeatNumber()));

            JPanel rowWrapperPanel = new JPanel();
            rowWrapperPanel.setLayout(new BoxLayout(rowWrapperPanel, BoxLayout.X_AXIS));
            rowWrapperPanel.setBackground(new Color(22, 22, 22));
            rowWrapperPanel.setMaximumSize(new Dimension(1100, 42));

            JLabel lblRowLetter = new JLabel(rowName, SwingConstants.LEFT);
            lblRowLetter.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblRowLetter.setForeground(Color.GRAY);
            lblRowLetter.setPreferredSize(new Dimension(40, 35));
            lblRowLetter.setMaximumSize(new Dimension(40, 35));
            rowWrapperPanel.add(lblRowLetter);
            rowWrapperPanel.add(Box.createRigidArea(new Dimension(40, 0))); 

            JPanel numericalRowGrid = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            numericalRowGrid.setBackground(new Color(22, 22, 22));

            for (Seat seat : rowSeats) {
                final int seatId = seat.getId();
                final int numLabel = seat.getSeatNumber();
                final String seatFullLabel = rowName + numLabel;
                final boolean isSoldOut = seat.isBooked();
                
                seatPriceMap.put(seatId, categoryPrice);

                if ((rowName.equals("L") || rowName.equals("K") || rowName.equals("J") || rowName.equals("H")) && numLabel == 14) {
                    numericalRowGrid.add(Box.createRigidArea(new Dimension(35, 0))); 
                }

                JButton btnSeatElement = new JButton(String.valueOf(numLabel)) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        Dimension sz = getSize();
                        boolean isSelected = selectedSeatIds.contains(seatId);

                        if (isSoldOut) {
                            g2.setColor(new Color(40, 40, 40));
                            g2.fillRoundRect(1, 1, sz.width - 2, sz.height - 2, 6, 6);
                            setForeground(new Color(80, 85, 90));
                        } else if (isSelected) {
                            g2.setColor(new Color(74, 194, 120)); 
                            g2.fillRoundRect(1, 1, sz.width - 2, sz.height - 2, 6, 6);
                            setForeground(Color.WHITE);
                        } else {
                            g2.setColor(new Color(25, 25, 25)); 
                            g2.fillRoundRect(1, 1, sz.width - 2, sz.height - 2, 6, 6);
                            g2.setColor(new Color(74, 194, 120, 160)); 
                            g2.drawRoundRect(1, 1, sz.width - 2, sz.height - 2, 6, 6);
                            setForeground(new Color(74, 194, 120));
                        }

                        FontMetrics fm = g2.getFontMetrics();
                        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                        g2.drawString(getText(), (sz.width - fm.stringWidth(getText())) / 2, (sz.height + fm.getAscent()) / 2 - 2);
                    }
                };

                btnSeatElement.setPreferredSize(new Dimension(36, 34));
                btnSeatElement.setContentAreaFilled(false);
                btnSeatElement.setBorderPainted(false);
                btnSeatElement.setFocusPainted(false);

                if (isSoldOut) {
                    btnSeatElement.setEnabled(false);
                } else {
                    btnSeatElement.addActionListener(e -> {
                        if (selectedSeatIds.contains(seatId)) {
                            selectedSeatIds.remove(seatId);
                            selectedSeatLabels.remove(seatId);
                            currentTotalAmount -= categoryPrice;
                        } else {
                            selectedSeatIds.add(seatId);
                            selectedSeatLabels.put(seatId, seatFullLabel);
                            currentTotalAmount += categoryPrice;
                        }
                        refreshDynamicCalculationLogBar();
                        btnSeatElement.repaint();
                    });
                }
                numericalRowGrid.add(btnSeatElement);
            }
            rowWrapperPanel.add(numericalRowGrid);
            mainLayoutContainer.add(rowWrapperPanel);
            mainLayoutContainer.add(Box.createRigidArea(new Dimension(0, 8))); 
        }
    }

    private void refreshDynamicCalculationLogBar() {
        int ticketCount = selectedSeatIds.size();
        lblTotalSummary.setText("Seats Selected: " + ticketCount + " " + selectedSeatLabels.values().toString() + "  |  Total Amount: INR " + String.format("%.2f", currentTotalAmount));
    }

    private void processCheckout() {
        if (selectedSeatIds.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Please select at least one seat to proceed with ticket checkout.", "No Selection Made", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        
        try {
            StringBuilder labels = new StringBuilder();
            for (String label : selectedSeatLabels.values()) {
                labels.append(label).append(" ");
            }
            
            String seatString = labels.toString().trim();
            if (bookingDAO.bookSelectedSeats(selectedSeatIds, currentUserId, currentShowId, currentTotalAmount, seatString)) {
                
                // Formulate Thermal Receipt Data Stream
                String receiptData = """
                    Booking Reference ID: #CG-%d
                    Show Identity Code  : %d
                    Allocated Seats     : %s
                    Total Cost Breakdown: INR %.2f
                    Transaction Status  : CONFIRMED & PAID
                    ----------------------------------
                    Thank you for choosing CineGrid!
                    """.formatted(new Random().nextInt(89999) + 10000, currentShowId, seatString, currentTotalAmount);
                
                // Check if user is ADMIN to allow direct thermal printing, else CUSTOMER download view
                boolean isAdmin = userRole != null && userRole.equalsIgnoreCase("ADMIN");
                
                // Trigger Visual Thermal Preview Window
                ReceiptPrinterUtil.showReceiptPreview(this, receiptData, isAdmin);
                
                this.dispose();
            } else { 
                JOptionPane.showMessageDialog(this, "Concurrency conflict detected. Some seats were processed in another parallel thread.", "Transaction Failure", JOptionPane.ERROR_MESSAGE); 
                loadSeatMatrixEngine(); 
            }
        } catch (SQLException ex) { 
            JOptionPane.showMessageDialog(this, "Database execution layer error: " + ex.getMessage(), "SQL Process Exception", JOptionPane.ERROR_MESSAGE); 
        }
    }
}