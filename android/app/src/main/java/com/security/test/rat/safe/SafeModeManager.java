package com.security.test.rat.safe;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Manages safe mode functionality for the Android RAT application
 * Provides a way to disable potentially harmful features
 */
public class SafeModeManager {
    
    private static final String TAG = "SafeModeManager";
    private static final String PREFS_NAME = "SafeModePrefs";
    private static final String KEY_SAFE_MODE = "safe_mode_enabled";
    private static final String KEY_SAFE_MODE_REASON = "safe_mode_reason";
    
    private static SafeModeManager instance;
    private final Context context;
    private final SharedPreferences sharedPreferences;
    
    private SafeModeManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get singleton instance of SafeModeManager
     */
    public static synchronized SafeModeManager getInstance(Context context) {
        if (instance == null) {
            instance = new SafeModeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Check if safe mode is currently enabled
     */
    public static boolean isSafeMode() {
        if (instance == null) {
            Log.w(TAG, "SafeModeManager not initialized, defaulting to safe mode");
            return true;
        }
        return instance.isSafeModeEnabled();
    }
    
    /**
     * Enable safe mode
     */
    public void enableSafeMode(String reason) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_SAFE_MODE, true);
            editor.putString(KEY_SAFE_MODE_REASON, reason);
            editor.apply();
            
            Log.i(TAG, "Safe mode enabled. Reason: " + reason);
        } catch (Exception e) {
            Log.e(TAG, "Error enabling safe mode", e);
        }
    }
    
    /**
     * Disable safe mode
     */
    public void disableSafeMode() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_SAFE_MODE, false);
            editor.remove(KEY_SAFE_MODE_REASON);
            editor.apply();
            
            Log.i(TAG, "Safe mode disabled");
        } catch (Exception e) {
            Log.e(TAG, "Error disabling safe mode", e);
        }
    }
    
    /**
     * Check if safe mode is enabled
     */
    public boolean isSafeModeEnabled() {
        return sharedPreferences.getBoolean(KEY_SAFE_MODE, true); // Default to safe mode
    }
    
    /**
     * Get the reason why safe mode was enabled
     */
    public String getSafeModeReason() {
        return sharedPreferences.getString(KEY_SAFE_MODE_REASON, "Unknown");
    }
    
    /**
     * Check if a specific feature is allowed in safe mode
     */
    public boolean isFeatureAllowed(String featureName) {
        if (!isSafeModeEnabled()) {
            return true; // All features allowed when not in safe mode
        }
        
        // Define safe features that are always allowed
        switch (featureName) {
            case "ui_display":
            case "settings_view":
            case "about_dialog":
            case "status_display":
                return true;
            default:
                Log.d(TAG, "Feature blocked in safe mode: " + featureName);
                return false;
        }
    }
    
    /**
     * Get safe mode status summary
     */
    public String getStatusSummary() {
        if (isSafeModeEnabled()) {
            return "Safe Mode: " + getSafeModeReason();
        } else {
            return "Normal Mode: All features enabled";
        }
    }
    
    /**
     * Reset safe mode settings to defaults
     */
    public void resetToDefaults() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            
            Log.i(TAG, "Safe mode settings reset to defaults");
        } catch (Exception e) {
            Log.e(TAG, "Error resetting safe mode settings", e);
        }
    }
}
