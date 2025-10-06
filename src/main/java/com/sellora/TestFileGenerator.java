package com.sellora;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TestFileGenerator {
    
    public static void main(String[] args) {
        createTestFiles();
    }
    
    public static void createTestFiles() {
        File testDir = new File("test-files");
        if (!testDir.exists()) {
            testDir.mkdirs();
            System.out.println("Created test-files directory");
        }
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Create different types of test files
        createTextFile(new File(testDir, "sample-document.txt"), 
            "Sample Document\n" +
            "================\n\n" +
            "This is a test document created on " + timestamp + "\n\n" +
            "This file can be used to test the Google Drive upload functionality.\n" +
            "The program will upload this file to your specified Google Drive folder.\n\n" +
            "Features being tested:\n" +
            "- File upload to Google Drive\n" +
            "- Automatic folder management\n" +
            "- File cleanup when limit exceeded\n" +
            "- Unique file naming with timestamps");
        
        createTextFile(new File(testDir, "notes.txt"), 
            "Notes\n" +
            "=====\n\n" +
            "Created: " + timestamp + "\n\n" +
            "These are sample notes for testing file upload.\n" +
            "This file will be uploaded to Google Drive and managed automatically.");
        
        createTextFile(new File(testDir, "data.csv"), 
            "Name,Age,City,Date\n" +
            "John Doe,25,New York," + timestamp + "\n" +
            "Jane Smith,30,Los Angeles," + timestamp + "\n" +
            "Bob Johnson,35,Chicago," + timestamp + "\n" +
            "Alice Brown,28,Houston," + timestamp);
        
        createTextFile(new File(testDir, "config.json"), 
            "{\n" +
            "  \"application\": \"Google Drive File Manager\",\n" +
            "  \"version\": \"1.0\",\n" +
            "  \"created\": \"" + timestamp + "\",\n" +
            "  \"settings\": {\n" +
            "    \"maxFiles\": 10,\n" +
            "    \"autoCleanup\": true,\n" +
            "    \"uniqueNaming\": true\n" +
            "  }\n" +
            "}");
        
        createTextFile(new File(testDir, "readme.md"), 
            "# Test File\n\n" +
            "This is a markdown test file created on " + timestamp + "\n\n" +
            "## Purpose\n" +
            "This file is used for testing the Google Drive upload and management system.\n\n" +
            "## Features\n" +
            "- Automatic upload\n" +
            "- File management\n" +
            "- Cleanup of old files\n\n" +
            "## Usage\n" +
            "Upload this file using the Google Drive File Manager application.");
        
        System.out.println("\n✅ Test files created successfully in 'test-files' directory:");
        System.out.println("   📄 sample-document.txt");
        System.out.println("   📄 notes.txt");
        System.out.println("   📊 data.csv");
        System.out.println("   ⚙️ config.json");
        System.out.println("   📝 readme.md");
        System.out.println("\nYou can now use these files to test the Google Drive upload functionality!");
        System.out.println("Run the main application and upload these files one by one to see the");
        System.out.println("automatic folder management in action.");
    }
    
    private static void createTextFile(File file, String content) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            System.out.println("Created: " + file.getName());
        } catch (IOException e) {
            System.err.println("Error creating file " + file.getName() + ": " + e.getMessage());
        }
    }
}