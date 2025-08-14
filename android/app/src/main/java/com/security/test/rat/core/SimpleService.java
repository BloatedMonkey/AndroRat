package com.security.test.rat.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Simple service for minimal build
 */
public class SimpleService extends Service {
    
    private static final String TAG = "SimpleService";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SimpleService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SimpleService started");
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SimpleService destroyed");
    }
}
