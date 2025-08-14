package com.security.test.rat.safe;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Safe Service - Educational stub for potentially harmful services
 * 
 * This service demonstrates how to safely implement service patterns
 * without enabling surveillance, control, or exfiltration capabilities.
 * 
 * Purpose: Educational and research only
 * Safety: No harmful operations, logging only
 */
public class SafeService extends Service {
    
    private static final String TAG = "SafeService";
    private boolean isRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "SafeService created - SAFE MODE ENABLED");
        Log.d(TAG, "This service is for educational purposes only");
        Log.d(TAG, "All potentially harmful operations are disabled");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "SafeService started - SAFE MODE ENABLED");
        
        if (intent != null) {
            String action = intent.getAction();
            Log.d(TAG, "Service started with action: " + action);
            
            if (action != null) {
                Log.d(TAG, "SAFE MODE: Action received but not executed");
                Log.d(TAG, "Action logged for educational purposes");
            }
        }
        
        isRunning = true;
        
        // Safe service operation - just log activity
        Log.i(TAG, "SafeService is running in SAFE MODE");
        Log.d(TAG, "Service ID: " + startId);
        Log.d(TAG, "Flags: " + flags);
        
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "SafeService bind requested");
        Log.d(TAG, "SAFE MODE: Binding not supported in safe mode");
        return null; // No binding in safe mode
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "SafeService destroyed");
        isRunning = false;
        Log.d(TAG, "SafeService cleanup completed");
    }
    
    /**
     * Check if service is running
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Get service status safely
     */
    public String getStatus() {
        return "SafeService Status: " + (isRunning ? "Running" : "Stopped") + 
               " (SAFE MODE - Educational purposes only)";
    }
    
    /**
     * Get educational information about this service
     */
    public String getEducationalInfo() {
        return "This is a SAFE SERVICE stub for educational purposes. " +
               "It demonstrates service patterns without enabling harmful functionality. " +
               "Use only for authorized testing and research.";
    }
    
    /**
     * Safe method for educational demonstrations
     */
    public void demonstrateSafeOperation() {
        Log.i(TAG, "Demonstrating safe operation");
        Log.d(TAG, "This method shows how to implement safe service operations");
        Log.d(TAG, "No harmful operations are performed");
        Log.i(TAG, "Safe operation demonstration completed");
    }
    
    /**
     * Cleanup the service safely
     */
    public void cleanup() {
        Log.i(TAG, "SafeService cleanup initiated");
        isRunning = false;
        Log.d(TAG, "SafeService cleanup completed");
    }
}
