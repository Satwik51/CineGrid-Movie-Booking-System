package com.cinegrid.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TransitionLoader {

    public static void showTransition(JFrame parent, String message, Runnable onComplete) {
        JDialog loadingDialog = new JDialog(parent, "", true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setSize(380, 180);
        loadingDialog.setLocationRelativeTo(parent);
        loadingDialog.setBackground(new Color(0, 0, 0, 0));

        // Custom Premium Painted Panel with Smooth Slow Cinematic Glow & Radar Animation
        JPanel panel = new JPanel() {
            private float angle = 0;
            private Timer paintTimer;

            {
                // Timer speed increased to 40ms for a slower, softer, and smoother rotation feel
                paintTimer = new Timer(40, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        angle += 4; // Reduced step size for gentle movement
                        if (angle >= 360) angle = 0;
                        repaint();
                    }
                });
                paintTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark Metallic Glassmorphism Background Box
                g2.setColor(new Color(18, 18, 22, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Outer Neon Border Frame
                g2.setColor(new Color(248, 68, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

                // Rotating Cinematic Scanner Ring at Top Center
                int cx = getWidth() / 2;
                int cy = 48;
                int radius = 22;

                g2.setColor(new Color(45, 45, 55));
                g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

                g2.setColor(new Color(248, 68, 100));
                g2.setStroke(new BasicStroke(3));
                g2.drawArc(cx - radius, cy - radius, radius * 2, radius * 2, (int)angle, 100);

                // Center glowing core dot
                g2.setColor(Color.WHITE);
                g2.fillOval(cx - 4, cy - 4, 8, 8);
                
                if (!isDisplayable() && paintTimer != null) {
                    paintTimer.stop();
                }
            }
        };

        panel.setLayout(null);
        panel.setBackground(new Color(0, 0, 0, 0));

        // Message Label
        JLabel lblMsg = new JLabel(message, SwingConstants.CENTER);
        lblMsg.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblMsg.setForeground(Color.WHITE);
        lblMsg.setBounds(20, 88, 340, 25);
        panel.add(lblMsg);

        // Sleek Progress Bar at Bottom
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(new Color(248, 68, 100));
        progressBar.setBackground(new Color(35, 35, 45));
        progressBar.setBounds(30, 125, 320, 6);
        panel.add(progressBar);

        loadingDialog.add(panel);

        // Slightly relaxed worker duration (~750ms) for a softer, unhurried transition feel
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(750); 
                return null;
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        };
        worker.execute();
        loadingDialog.setVisible(true);
    }
}