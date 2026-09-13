package com.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@SpringBootApplication
public class ChatbotApplication {

    public static void main(String[] args) {
        // Thiết lập chuẩn UTF-8 cho console output trên Windows
        initUtf8Console();

        // Tự động nạp file .env từ thư mục gốc nếu có (giống Node.js dotenv / loadEnv trong server.js)
        loadDotEnv();

        SpringApplication.run(ChatbotApplication.class, args);
    }

    private static void initUtf8Console() {
        System.setProperty("file.encoding", "UTF-8");
        try {
            System.setOut(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out), true, "UTF-8"));
            System.setErr(new java.io.PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.err), true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException ignored) {
        }
    }

    private static void loadDotEnv() {
        File[] candidates = new File[] {
                new File(".env"),
                new File("../.env"),
                new File(System.getProperty("user.dir"), ".env")
        };

        for (File candidate : candidates) {
            if (candidate.exists() && candidate.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(candidate))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;

                        int eqIdx = line.indexOf('=');
                        if (eqIdx != -1) {
                            String key = line.substring(0, eqIdx).trim();
                            String val = line.substring(eqIdx + 1).trim();

                            if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                                val = val.substring(1, val.length() - 1);
                            } else if (val.startsWith("'") && val.endsWith("'") && val.length() >= 2) {
                                val = val.substring(1, val.length() - 1);
                            } else {
                                int hashIdx = val.indexOf('#');
                                if (hashIdx != -1) {
                                    val = val.substring(0, hashIdx).trim();
                                }
                            }

                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                    System.out.println("[ChatbotApplication] Đã nạp biến môi trường từ: " + candidate.getAbsolutePath());
                    break;
                } catch (IOException e) {
                    System.err.println("[ChatbotApplication] Không thể đọc file .env: " + e.getMessage());
                }
            }
        }
    }
}
