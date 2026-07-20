package com.cinegrid.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConfig {
    public static Connection getConnection() {
        try {
            Properties prop = new Properties();
            // Project root folder se config.properties file read kar rahe hain
            try (InputStream input = new FileInputStream("config.properties")) {
                prop.load(input);
            } catch (Exception e) {
                // Fallback agar classpath se read karna pade
                InputStream cpInput = DBConfig.class.getClassLoader().getResourceAsStream("config.properties");
                if (cpInput != null) {
                    prop.load(cpInput);
                }
            }

            String host = prop.getProperty("db.host", "mysql-300b296b-cinegrid-project.a.aivencloud.com");
            String port = prop.getProperty("db.port", "21804");
            String dbName = prop.getProperty("db.name", "defaultdb");
            String user = prop.getProperty("db.user", "avnadmin");
            String pass = prop.getProperty("db.pass"); // Yeh ab config.properties se aayega

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?sslMode=REQUIRED&serverTimezone=Asia/Kolkata";
            
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}