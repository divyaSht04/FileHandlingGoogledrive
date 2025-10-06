# Google Drive File Manager Setup Instructions

## Prerequisites
1. **Java 11 or higher** - # Google Drive File Manager

A Java application that uploads files to Google Drive and automatically manages folder contents by maintaining a maximum of 10 files, deleting the oldest files when the limit is exceeded.

## Features

- Upload files to a specific Google Drive folder
- Automatically maintain maximum 10 files per folder
- Delete oldest files when limit is exceeded (based on creation time)
- Interactive command-line interface
- Support for various file types
- Unique filename generation with timestamps

## Prerequisites

1. **Java 24** or higher
   2. **Maven 3.6+**
3. **Google Cloud Console project** with Drive API enabled
4. **OAuth 2.0 credentials** for desktop application

## Setup Instructions

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable the Google Drive API:
   - Go to "APIs & Services" > "Library"
   - Search for "Google Drive API"
   - Click "Enable"

### 2. Create OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth client ID"
3. **Important**: Choose "Desktop application" (NOT Web application)
4. Give it a name (e.g., "Drive File Manager")
5. **Add Authorized redirect URIs**:
   - Click "Add URI"
   - Add: `http://localhost:8888/Callback`
   - Add: `http://localhost:8888`
6. Click "Create"
7. Download the JSON file
8. Rename it to `credentials.json`
9. Place it in `src/main/resources/credentials.json`

### 3. Configure OAuth Consent Screen (Important!)

1. Go to "APIs & Services" > "OAuth consent screen"
2. Choose "External" (unless you have Google Workspace)
3. Fill in required fields:
   - App name: "Drive File Manager"
   - User support email: Your email
   - Developer contact: Your email
4. **Add your email to Test users**:
   - Go to "Test users" section
   - Click "Add users"
   - Add your Google account email
5. Save and continue through all steps

### 4. Build and Run

```bash
# Clone or download the project
cd FileHandlingGdrive

# Compile the project
mvn clean compile

# Run the application
mvn exec:java -Dexec.mainClass="com.sellora.Main"
```

## Usage

1. **Run the application**:
   ```bash
   mvn exec:java -Dexec.mainClass="com.sellora.Main"
   ```

2. **First-time authorization**:
   - Browser will open for Google authentication
   - Grant permissions to access Google Drive
   - Authorization token will be saved locally

3. **Upload files**:
   - Choose option 1 from the menu
   - Enter the full path to the file you want to upload
   - The file will be uploaded with a timestamp suffix

4. **Automatic file management**:
   - After each upload, the system checks folder contents
   - If more than 10 files exist, oldest files are automatically deleted
   - You'll see which files were deleted in the console output

## Project Structure

```
src/
├── main/
│   ├── java/com/sellora/
│   │   ├── GoogleDriveService.java  # Main Google Drive API logic
│   │   ├── Main.java                # Interactive CLI application
│   │   └── TestFileGenerator.java   # Utility for creating test files
│   └── resources/
│       └── credentials.json         # Your OAuth credentials (not in repo)
├── test/
└── pom.xml                         # Maven dependencies
```

## Key Classes

### GoogleDriveService
- Handles Google Drive API authentication
- Manages file uploads and deletions
- Implements folder content management logic
- Provides file listing capabilities

### Main
- Interactive command-line interface
- User input handling
- Demonstrates the upload and management workflow

## Configuration

- **Maximum files per folder**: 10 (configurable in `GoogleDriveService.MAX_FILES_IN_FOLDER`)
- **Default folder name**: "myfiles" (can be changed during runtime)
- **Authorization port**: 8888 (for OAuth callback)

## File Management Logic

1. Files are sorted by creation time (oldest first)
2. When folder exceeds 10 files, oldest files are deleted
3. Each uploaded file gets a unique name with timestamp
4. Deleted files are permanently removed from Google Drive

## Troubleshooting

### Common Issues

1. **"Error 403: access_denied"**
   - **Most Common Cause**: OAuth client type is wrong
   - **Solution**: Make sure you selected "Desktop application", NOT "Web application"
   - **Check redirect URIs**: Must include `http://localhost:8888/Callback`
   - **Add yourself as test user** in OAuth consent screen
   - **Delete tokens folder** and try again: `rm -rf tokens/` (Windows: `rmdir /s tokens`)

2. **"credentials.json not found"**
   - Ensure the file is in `src/main/resources/`
   - Check file name is exactly `credentials.json`

3. **Authentication fails**
   - Check if Google Drive API is enabled
   - Verify OAuth client type is "Desktop application"
   - Ensure redirect URI is configured correctly

4. **Compilation errors**
   - Verify Java 24 is installed and set as JAVA_HOME
   - Run `mvn clean compile` to check for issues

### Debug Mode

Run with debug logging:
```bash
mvn exec:java -Dexec.mainClass="com.sellora.Main" -X
```

## Security Notes

- `credentials.json` contains sensitive information - never commit to version control
- Tokens are stored locally in `tokens/` directory
- Application only requests necessary Drive permissions

## License

This project is for educational/demonstration purposes.
2. **Maven** - For dependency management
3. **Google Cloud Platform Account** - To access Google Drive API

## Setup Steps

### 1. Create Google Cloud Project and Enable Drive API

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Google Drive API:
   - Go to "APIs & Services" > "Library"
   - Search for "Google Drive API"
   - Click on it and press "Enable"

### 2. Create OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth client ID"
3. If prompted, configure the OAuth consent screen first:
   - Choose "External" user type
   - Fill in the required fields (App name, User support email, etc.)
   - Add your email to test users
4. For Application type, select "Desktop application"
5. Give it a name (e.g., "Google Drive File Manager")
6. Click "Create"
7. Download the credentials JSON file

### 3. Setup Credentials File

1. Rename the downloaded credentials file to `credentials.json`
2. Place it in the `src/main/resources/` directory of your project
3. The file structure should be:
   ```
   src/
     main/
       resources/
         credentials.json  <-- Place your file here
   ```

### 4. Build and Run

1. Open terminal in the project directory
2. Install dependencies:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn exec:java -Dexec.mainClass="com.sellora.Main"
   ```

### 5. First Run Authentication

1. The first time you run the application, it will:
   - Open your default web browser
   - Ask you to sign in to your Google account
   - Request permission to access your Google Drive
   - Save authentication tokens locally for future use

## How It Works

### File Upload Process:
1. **Upload File**: Select and upload a file to the specified Google Drive folder
2. **Check File Count**: After upload, check if folder has more than 10 files
3. **Auto-Cleanup**: If more than 10 files exist, delete the oldest files to maintain the limit
4. **Unique Naming**: Files are renamed with timestamps to avoid conflicts

### Features:
- ✅ Upload files to specific Google Drive folder
- ✅ Automatic folder creation if it doesn't exist
- ✅ Maintain maximum 10 files per folder
- ✅ Delete oldest files when limit exceeded
- ✅ Unique file naming with timestamps
- ✅ File size display and validation
- ✅ Interactive command-line interface

### File Naming:
Files are automatically renamed to include timestamps:
- `document.pdf` becomes `document_20241005_143022.pdf`
- This prevents naming conflicts and makes files easily identifiable

## Troubleshooting

### Common Issues:

1. **"Resource not found: /credentials.json"**
   - Make sure `credentials.json` is in `src/main/resources/` directory
   - Check the file name is exactly `credentials.json`

2. **Authentication errors**
   - Delete the `tokens` directory and re-authenticate
   - Make sure your Google account has Drive access enabled

3. **Permission denied**
   - Check OAuth consent screen configuration
   - Ensure your email is added as a test user

4. **Build failures**
   - Ensure Java 11+ is installed
   - Check Maven installation
   - Run `mvn clean install` to refresh dependencies

## Security Notes

- Never commit `credentials.json` to version control
- The `tokens` directory contains sensitive authentication data
- Add both to your `.gitignore` file:
  ```
  src/main/resources/credentials.json
  tokens/
  ```

## Usage Example

```
=== Google Drive File Manager ===
Enter the folder name (default: 'myfiles'): myfiles
Working with folder: myfiles (ID: 1abc123def456)

=== Files in folder ===
Total files: 8

Choose an option:
1. Upload a file
2. List files in folder
3. Exit
Enter your choice (1-3): 1

Enter the path to the file you want to upload: C:\Users\YourName\Desktop\document.pdf
Uploading file: document.pdf (2.5 MB)
File uploaded successfully:
  Name: document_20241005_143022.pdf
  ID: 1xyz789abc123

Current files in folder: 9
Upload complete!
```