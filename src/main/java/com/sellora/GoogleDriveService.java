package com.sellora;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GoogleDriveService {
    private static final String APPLICATION_NAME = "Google Drive Java File Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final int MAX_FILES_IN_FOLDER = 10;

    private final Drive service;

    public GoogleDriveService() throws IOException, GeneralSecurityException {
        this.service = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials() throws IOException, GeneralSecurityException {
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .setCallbackPath("/Callback")
                .build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Creates a folder in Google Drive if it doesn't exist
     * @param folderName The name of the folder to create
     * @return The folder ID
     * @throws IOException
     */
    public String createOrGetFolder(String folderName) throws IOException {
        String pageToken = null;
        do {
            FileList result = service.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder' and name='" + folderName + "' and trashed=false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            for (File file : result.getFiles()) {
                if (file.getName().equals(folderName)) {
                    System.out.println("Found existing folder: " + folderName + " (ID: " + file.getId() + ")");
                    return file.getId();
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        // Create new folder if not found
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        File folder = service.files()
                .create(fileMetadata)
                .setFields("id").execute();
        System.out.println("Created new folder: " + folderName + " (ID: " + folder.getId() + ")");
        return folder.getId();
    }

    /**
     * Uploads a file to a specific folder in Google Drive
     * @param localFilePath Path to the local file
     * @param folderId The Google Drive folder ID where the file should be uploaded
     * @return The uploaded file ID
     * @throws IOException
     */
    public String uploadFile(String localFilePath, String folderId) throws IOException {
        java.io.File localFile = new java.io.File(localFilePath);
        if (!localFile.exists()) {
            throw new FileNotFoundException("Local file not found: " + localFilePath);
        }

        // Generate unique filename with timestamp
        String fileName = generateUniqueFileName(localFile.getName());
        
        File fileMetadata = new File();
        fileMetadata.setName(fileName);
        fileMetadata.setParents(Collections.singletonList(folderId));

        FileContent mediaContent = new FileContent(getMimeType(localFile), localFile);

        File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id, name, createdTime")
                .execute();

        System.out.println("File uploaded successfully:");
        System.out.println("  Name: " + uploadedFile.getName());
        System.out.println("  ID: " + uploadedFile.getId());

        return uploadedFile.getId();
    }

    /**
     * Manages files in a folder to ensure no more than MAX_FILES_IN_FOLDER exist
     * Deletes oldest files if limit is exceeded
     * @param folderId The folder ID to manage
     * @throws IOException
     */
    public void manageFilesInFolder(String folderId) throws IOException {
        List<File> files = getFilesInFolder(folderId);
        
        System.out.println("Current files in folder: " + files.size());
        
        if (files.size() > MAX_FILES_IN_FOLDER) {
            // Sort files by creation time (oldest first)
            files.sort((f1, f2) -> {
                try {
                    return Long.compare(f1.getCreatedTime().getValue(), f2.getCreatedTime().getValue());
                } catch (Exception e) {
                    return 0;
                }
            });

            int filesToDelete = files.size() - MAX_FILES_IN_FOLDER;
            System.out.println("Need to delete " + filesToDelete + " oldest files to maintain limit of " + MAX_FILES_IN_FOLDER);

            for (int i = 0; i < filesToDelete; i++) {
                File fileToDelete = files.get(i);
                deleteFile(fileToDelete.getId());
                System.out.println("Deleted: " + fileToDelete.getName() + " (Created: " + fileToDelete.getCreatedTime() + ")");
            }
        }
    }

    /**
     * Gets all files in a specific folder
     * @param folderId The folder ID
     * @return List of files in the folder
     * @throws IOException
     */
    private List<File> getFilesInFolder(String folderId) throws IOException {
        List<File> files = new ArrayList<>();
        String pageToken = null;

        do {
            FileList result = service.files().list()
                    .setQ("'" + folderId + "' in parents and trashed=false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, createdTime, mimeType)")
                    .setPageToken(pageToken)
                    .execute();

            files.addAll(result.getFiles());
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return files;
    }

    /**
     * Deletes a file from Google Drive
     * @param fileId The file ID to delete
     * @throws IOException
     */
    private void deleteFile(String fileId) throws IOException {
        service.files().delete(fileId).execute();
    }

    /**
     * Lists all files in a folder with their details
     * @param folderId The folder ID
     * @throws IOException
     */
    public void listFilesInFolder(String folderId) throws IOException {
        List<File> files = getFilesInFolder(folderId);
        
        System.out.println("\n=== Files in folder ===");
        System.out.println("Total files: " + files.size());
        
        if (files.isEmpty()) {
            System.out.println("No files found in the folder.");
            return;
        }

        files.sort((f1, f2) -> Long.compare(f2.getCreatedTime().getValue(), f1.getCreatedTime().getValue()));

        System.out.println("Files (sorted by creation time - newest first):");
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            System.out.println((i + 1) + ". " + file.getName() + " (Created: " + file.getCreatedTime() + ")");
        }
        System.out.println("========================\n");
    }

    /**
     * Generates a unique filename with timestamp
     * @param originalName The original filename
     * @return A unique filename with timestamp
     */
    private String generateUniqueFileName(String originalName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        int lastDotIndex = originalName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String nameWithoutExtension = originalName.substring(0, lastDotIndex);
            String extension = originalName.substring(lastDotIndex);
            return nameWithoutExtension + "_" + timestamp + extension;
        } else {
            return originalName + "_" + timestamp;
        }
    }

    /**
     * Gets the MIME type of a file
     * @param file The file
     * @return The MIME type
     */
    private String getMimeType(java.io.File file) {
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".txt")) return "text/plain";
        if (fileName.endsWith(".pdf")) return "application/pdf";
        if (fileName.endsWith(".doc")) return "application/msword";
        if (fileName.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".gif")) return "image/gif";
        if (fileName.endsWith(".zip")) return "application/zip";
        
        return "application/octet-stream"; // Default binary type
    }
}