package com.sellora;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            GoogleDriveService driveService = new GoogleDriveService();
            Scanner scanner = new Scanner(System.in);
            
            System.out.println("=== Google Drive File Manager ===");
            System.out.println("This program will:");
            System.out.println("1. Upload files to a specific Google Drive folder");
            System.out.println("2. Automatically maintain maximum 10 files in the folder");
            System.out.println("3. Delete oldest files when limit is exceeded\n");
            
            // Get or create the target folder
            System.out.print("Enter the folder name (default: 'myfiles'): ");
            String folderName = scanner.nextLine().trim();
            if (folderName.isEmpty()) {
                folderName = "myfiles";
            }
            
            String folderId = driveService.createOrGetFolder(folderName);
            System.out.println("Working with folder: " + folderName + " (ID: " + folderId + ")\n");

            driveService.listFilesInFolder(folderId);
            
            while (true) {
                System.out.println("Choose an option:");
                System.out.println("1. Upload a file");
                System.out.println("2. List files in folder");
                System.out.println("3. Exit");
                System.out.print("Enter your choice (1-3): ");
                
                String choice = scanner.nextLine().trim();
                
                switch (choice) {
                    case "1":
                        uploadFileFlow(driveService, folderId, scanner);
                        break;
                    case "2":
                        driveService.listFilesInFolder(folderId);
                        break;
                    case "3":
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please enter 1, 2, or 3.\n");
                }
            }
            
        } catch (IOException | GeneralSecurityException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void uploadFileFlow(GoogleDriveService driveService, String folderId, Scanner scanner) {
        try {
            System.out.print("Enter the path to the file you want to upload: ");
            String filePath = scanner.nextLine().trim();
            
            // Remove quotes if present
            if (filePath.startsWith("\"") && filePath.endsWith("\"")) {
                filePath = filePath.substring(1, filePath.length() - 1);
            }
            
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Error: File not found at " + filePath + "\n");
                return;
            }
            
            if (file.isDirectory()) {
                System.out.println("Error: Please specify a file, not a directory.\n");
                return;
            }
            
            System.out.println("Uploading file: " + file.getName() + " (" + formatFileSize(file.length()) + ")");

            String uploadedFileId = driveService.uploadFile(filePath, folderId);

            driveService.manageFilesInFolder(folderId);
            
            System.out.println("Upload complete!\n");

            driveService.listFilesInFolder(folderId);
            
        } catch (IOException e) {
            System.err.println("Error uploading file: " + e.getMessage() + "\n");
        }
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}