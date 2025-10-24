package com.example.unimatcher.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseChecker {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void checkConnection() {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Conexiune reușită la baza de date!");
            System.out.println("🔗 URL: " + conn.getMetaData().getURL());
            System.out.println("👤 User: " + conn.getMetaData().getUserName());
            System.out.println("💾 Driver: " + conn.getMetaData().getDriverName());
            System.out.println("🧩 DB: " + conn.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            System.out.println("❌ Eroare la conexiunea cu baza de date: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
