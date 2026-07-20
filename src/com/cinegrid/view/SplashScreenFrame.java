package com.cinegrid.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SplashScreenFrame extends JFrame {
    private JProgressBar progressBar;
    private JLabel lblStatus;
    private float alpha = 0.0f;
    private int reelAngle = 0; // Cinematic rotating film reel animation angle
    private Timer animTimer;

    public SplashScreenFrame() {
        setUndecorated(true);
        setSize(650, 400);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(15, 15, 15));
        setLayout(new BorderLayout());

        // Center Panel with Custom Cinematic Film Projector & Logo Graphics
        JPanel centerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Smooth Alpha Fade-In
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha, 1.0f)));

                int centerX = getWidth() / 2;
                int centerY = 110;

                // --- CINEMATIC ROTATING FILM REEL GRAPHIC ---
                g2.setColor(new Color(40, 40, 50));
                g2.fillOval(centerX - 45, centerY - 45, 90, 90);
                
                g2.setColor(new Color(248, 68, 100));
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(centerX - 45, centerY - 45, 90, 90);

                // Rotating holes/spokes inside the reel
                g2.rotate(Math.toRadians(reelAngle), centerX, centerY);
                for (int i = 0; i < 6; i++) {
                    double angle = i * Math.PI / 3;
                    int hx = centerX + (int)(28 * Math.cos(angle)) - 8;
                    int hy = centerY + (int)(28 * Math.sin(angle)) - 8;
                    g2.setColor(new Color(15, 15, 15));
                    g2.fillOval(hx, hy, 16, 16);
                    g2.setColor(new Color(248, 68, 100));
                    g2.drawOval(hx, hy, 16, 16);
                }
                g2.rotate(-Math.toRadians(reelAngle), centerX, centerY); // Reset rotation

                // Inner Hub
                g2.setColor(new Color(248, 68, 100));
                g2.fillOval(centerX - 12, centerY - 12, 24, 24);
                g2.setColor(Color.WHITE);
                g2.fillOval(centerX - 4, centerY - 4, 8, 8);

                // --- CINEGRID BRANDING TEXT ---
                g2.setFont(new Font("SansSerif", Font.BOLD, 28));
                String title = "CINEGRID";
                int textWidth = g2.getFontMetrics().stringWidth(title);
                g2.setColor(Color.WHITE);
                g2.drawString(title, (getWidth() - textWidth) / 2, centerY + 75);

                // Subtitle Tagline
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                g2.setColor(new Color(170, 170, 170));
                String tagline = "ENTERPRISE MULTIPLEX CINEMA ENGINE";
                int tagWidth = g2.getFontMetrics().stringWidth(tagline);
                g2.drawString(tagline, (getWidth() - tagWidth) / 2, centerY + 100);
            }
        };
        centerPanel.setBackground(new Color(15, 15, 15));
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Loading Bar Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(15, 15, 15));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 35, 35, 35));

        lblStatus = new JLabel("Initializing Secure Gatekeeper Modules...");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblStatus.setForeground(Color.LIGHT_GRAY);
        bottomPanel.add(lblStatus, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(580, 8));
        progressBar.setForeground(new Color(248, 68, 100));
        progressBar.setBackground(new Color(35, 35, 35));
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // Animation Timer (~30 FPS for smooth reel rotation & fade-in)
        animTimer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (alpha < 1.0f) {
                    alpha += 0.05f;
                }
                reelAngle += 6; // Spin speed of the film reel
                if (reelAngle >= 360) reelAngle = 0;
                centerPanel.repaint();
            }
        });
        animTimer.start();

        startLoadingAnimation();
    }

    private void startLoadingAnimation() {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String[] statuses = {
                    "Loading Database Connection Matrix...",
                    "Calibrating Secure Thermal Receipt Engine...",
                    "Configuring Secondary Movie Formats (IMAX/4DX)...",
                    "Launching CineGrid Gateway..."
                };
                
                for (int i = 0; i <= 100; i += 2) {
                    Thread.sleep(32);
                    publish(i);
                    
                    if (i == 25) updateStatus(statuses[0]);
                    else if (i == 50) updateStatus(statuses[1]);
                    else if (i == 75) updateStatus(statuses[2]);
                    else if (i == 95) updateStatus(statuses[3]);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int val = chunks.get(chunks.size() - 1);
                progressBar.setValue(val);
            }

            @Override
            protected void done() {
                if (animTimer != null) animTimer.stop();
                
                try {
                    Thread.sleep(120);
                } catch (InterruptedException ignored) {}

                dispose();
                new AuthFrame().setVisible(true);
            }
        };
        worker.execute();
    }

    private void updateStatus(String text) {
        SwingUtilities.invokeLater(() -> lblStatus.setText(text));
    }
}