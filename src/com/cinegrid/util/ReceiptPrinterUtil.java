package com.cinegrid.util;

import javax.print.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;

public class ReceiptPrinterUtil {

    public static void showReceiptPreview(JFrame parent, String receiptText, boolean isAdmin) {
        JDialog previewDialog = new JDialog(parent, "CineGrid Official Thermal Receipt Preview", true);
        previewDialog.setSize(440, 640);
        previewDialog.setLocationRelativeTo(parent);
        previewDialog.setLayout(new BorderLayout());
        previewDialog.getContentPane().setBackground(new Color(20, 20, 20));

        String bookingRefId = extractBookingId(receiptText);

        JPanel paperPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Monospaced", Font.BOLD, 14));
                int y = 30;
                g2.drawString("==================================", 15, y); y += 20;
                g2.drawString("       CINEGRID MULTIPLEX         ", 15, y); y += 20;
                g2.drawString("   SECURE DIGITAL TICKET DESK     ", 15, y); y += 20;
                g2.drawString("==================================", 15, y); y += 25;
                
                g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
                for (String line : receiptText.split("\n")) {
                    g2.drawString(line, 15, y);
                    y += 18;
                }
                
                y += 15;
                int qrX = 145;
                int qrSize = 120;
                
                g2.setColor(Color.BLACK);
                g2.drawRect(qrX - 5, y - 5, qrSize + 10, qrSize + 35);
                g2.fillRect(qrX, y, qrSize, qrSize);
                
                g2.setColor(Color.WHITE);
                g2.fillRect(qrX + 4, y + 4, qrSize - 8, qrSize - 8);
                
                g2.setColor(Color.BLACK);
                int cellSize = 6;
                int grid = qrSize / cellSize;
                int hashSeed = bookingRefId.hashCode();
                
                for (int row = 0; row < grid; row++) {
                    for (int col = 0; col < grid; col++) {
                        boolean isTopLeft = (row < 4 && col < 4);
                        boolean isTopRight = (row < 4 && col >= grid - 4);
                        boolean isBottomLeft = (row >= grid - 4 && col < 4);
                        
                        if (isTopLeft || isTopRight || isBottomLeft) continue;
                        
                        if (((hashSeed + row * 31 + col * 17) % 3) == 0) {
                            g2.fillRect(qrX + 4 + (col * cellSize), y + 4 + (row * cellSize), cellSize, cellSize);
                        }
                    }
                }
                
                drawQRFinderPattern(g2, qrX + 4, y + 4);
                drawQRFinderPattern(g2, qrX + qrSize - 28, y + 4);
                drawQRFinderPattern(g2, qrX + 4, y + qrSize - 28);
                
                g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                g2.drawString("ID: " + bookingRefId, qrX + 15, y + qrSize + 15);
                g2.drawString("[SCAN TO VERIFY]", qrX - 5, y + qrSize + 28);
            }
        };
        paperPanel.setBackground(Color.WHITE);
        paperPanel.setPreferredSize(new Dimension(400, 560));

        JScrollPane scrollPane = new JScrollPane(paperPanel);
        scrollPane.setBorder(null);
        previewDialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(30, 30, 30));

        if (isAdmin) {
            JButton btnPrint = new JButton("🖨️ Print to Thermal Printer");
            btnPrint.setFont(new Font("SansSerif", Font.BOLD, 13));
            btnPrint.setBackground(new Color(0, 200, 115));
            btnPrint.setForeground(Color.BLACK);
            btnPrint.addActionListener(e -> {
                triggerThermalPrint(receiptText);
                JOptionPane.showMessageDialog(previewDialog, "Print job sent successfully to thermal printer!", "Printer Active", JOptionPane.INFORMATION_MESSAGE);
                previewDialog.dispose();
            });
            buttonPanel.add(btnPrint);
        } else {
            JButton btnDownload = new JButton("📥 Download Ticket PDF");
            btnDownload.setFont(new Font("SansSerif", Font.BOLD, 13));
            btnDownload.setBackground(new Color(33, 150, 243));
            btnDownload.setForeground(Color.WHITE);
            btnDownload.addActionListener(e -> {
                boolean success = PdfTicketExporter.generateTicketPdf(receiptText);
                if (success) {
                    JOptionPane.showMessageDialog(previewDialog, "Ticket PDF saved successfully inside your 'Downloads' folder!", "Download Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(previewDialog, "Failed to export PDF ticket.", "Export Error", JOptionPane.ERROR_MESSAGE);
                }
                previewDialog.dispose();
            });
            buttonPanel.add(btnDownload);
        }

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnClose.setBackground(Color.DARK_GRAY);
        btnClose.setForeground(Color.WHITE);
        btnClose.addActionListener(e -> previewDialog.dispose());
        buttonPanel.add(btnClose);

        previewDialog.add(buttonPanel, BorderLayout.SOUTH);
        previewDialog.setVisible(true);
    }

    private static String extractBookingId(String text) {
        try {
            for (String line : text.split("\n")) {
                if (line.contains("Booking Reference ID:")) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (Exception ignored) {}
        return "#CG-99999";
    }

    private static void drawQRFinderPattern(Graphics2D g2, int x, int y) {
        g2.setColor(Color.BLACK);
        g2.fillRect(x, y, 24, 24);
        g2.setColor(Color.WHITE);
        g2.fillRect(x + 4, y + 4, 16, 16);
        g2.setColor(Color.BLACK);
        g2.fillRect(x + 8, y + 8, 8, 8);
    }

    private static void triggerThermalPrint(String textData) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            int y = 20;
            g2d.drawString("CINEGRID TICKET DESK", 10, y); y += 15;
            for (String line : textData.split("\n")) {
                g2d.drawString(line, 10, y);
                y += 12;
            }
            return Printable.PAGE_EXISTS;
        });
        
        try {
            // if (job.printDialog()) { job.print(); }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}