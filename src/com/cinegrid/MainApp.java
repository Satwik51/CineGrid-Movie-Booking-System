package com.cinegrid;

import com.cinegrid.view.SplashScreenFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MainApp {
    public static void main(String[] args) {
        // Set cross-platform look and feel configuration
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Launch Cinematic Splash Screen Animation as entry point
        SwingUtilities.invokeLater(() -> {
            new SplashScreenFrame().setVisible(true);
        });
    }
}