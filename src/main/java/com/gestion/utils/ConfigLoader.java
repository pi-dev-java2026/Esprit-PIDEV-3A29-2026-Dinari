package com.gestion.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {

    private static final String[] PROPERTY_FILES = {
            "local.properties",
            ".env",
            "config.properties"
    };

    public static String getApiKey(String keyName) {
        String userDir = System.getProperty("user.dir");

        System.out.println("=== Configuration Loader ===");
        System.out.println("Working directory (user.dir): " + userDir);
        System.out.println("Looking for key name: " + keyName);

        // Guard: if someone accidentally passes the actual key instead of the key name
        // (e.g., getApiKey("gsk_...")), detect and return it.
        if (looksLikeApiKey(keyName)) {
            System.out.println("! Warning: getApiKey() received an API key value instead of a key name.");
            String cleaned = cleanValue(keyName);
            System.out.println("✓ Using provided API key value (masked): " + maskApiKey(cleaned));
            return cleaned;
        }

        // 1) System property (from Maven -D flag)
        String key = cleanValue(System.getProperty(keyName));
        if (isPresent(key)) {
            System.out.println("✓ API Key loaded from: System Property (-D" + keyName + "=...)");
            System.out.println("✓ Key value (masked): " + maskApiKey(key));
            return key;
        }

        // 2) Environment variable
        key = cleanValue(System.getenv(keyName));
        if (isPresent(key)) {
            System.out.println("✓ API Key loaded from: Environment Variable (" + keyName + ")");
            System.out.println("✓ Key value (masked): " + maskApiKey(key));
            return key;
        }

        // 3) Properties files
        key = loadFromPropertiesFiles(keyName, userDir);
        if (isPresent(key)) {
            return key;
        }

        // 4) Resources folder
        key = loadFromResources(keyName);
        if (isPresent(key)) {
            System.out.println("✓ API Key loaded from: Resources (application.properties)");
            System.out.println("✓ Key value (masked): " + maskApiKey(key));
            return key;
        }

        System.err.println("✗ API Key NOT FOUND!");
        System.err.println("Checked locations:");
        System.err.println("  1. System Property: -D" + keyName + "=...");
        System.err.println("  2. Environment Variable: " + keyName);
        for (String fileName : PROPERTY_FILES) {
            Path filePath = Paths.get(userDir, fileName);
            System.err.println("  3. File: " + filePath.toAbsolutePath());
        }
        System.err.println("  4. Resources: application.properties");
        System.err.println();
        System.err.println("To fix: create local.properties in: " + userDir);
        System.err.println("Content: " + keyName + "=your_key_here");

        return null;
    }

    private static String loadFromPropertiesFiles(String keyName, String userDir) {
        for (String fileName : PROPERTY_FILES) {
            Path filePath = Paths.get(userDir, fileName);
            System.out.println("Checking file: " + filePath.toAbsolutePath());

            if (!Files.exists(filePath)) {
                System.out.println("  ✗ File does not exist");
                continue;
            }

            try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                Properties props = new Properties();
                props.load(fis);

                String value = cleanValue(props.getProperty(keyName));

                if (isPresent(value)) {
                    System.out.println("  ✓ File exists and contains key");
                    System.out.println("✓ API Key loaded from: " + fileName);
                    System.out.println("✓ Full path: " + filePath.toAbsolutePath());
                    System.out.println("✓ Key value (masked): " + maskApiKey(value));
                    return value;
                } else {
                    System.out.println("  ✗ File exists but key not found or empty");
                }
            } catch (IOException e) {
                System.out.println("  ✗ Error reading file: " + e.getMessage());
            }
        }
        return null;
    }

    private static String loadFromResources(String keyName) {
        try (InputStream is = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {

            if (is == null) return null;

            Properties props = new Properties();
            props.load(is);
            return cleanValue(props.getProperty(keyName));

        } catch (IOException e) {
            return null;
        }
    }

    private static boolean isPresent(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String cleanValue(String s) {
        if (s == null) return null;
        String v = s.trim();

        // remove wrapping quotes if present: "..." or '...'
        if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
            v = v.substring(1, v.length() - 1).trim();
        }
        return v;
    }

    private static boolean looksLikeApiKey(String s) {
        if (s == null) return false;
        String v = s.trim();
        // Groq keys typically start with gsk_
        return v.startsWith("gsk_") && v.length() >= 20;
    }

    private static String maskApiKey(String key) {
        if (key == null || key.length() < 10) return "***";
        return key.substring(0, 7) + "..." + key.substring(key.length() - 4);
    }
}