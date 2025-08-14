package com.security.test.rat.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages persistent data storage for the Android RAT application
 * Handles SharedPreferences, file storage, and data serialization
 */
public class PersistenceManager {
    
    private static final String TAG = "PersistenceManager";
    private static final String PREFS_NAME = "AndroidRATPrefs";
    
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    public PersistenceManager(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Save a string value to SharedPreferences
     */
    public void saveString(String key, String value) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
            Log.d(TAG, "Saved string: " + key + " = " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error saving string: " + key, e);
        }
    }
    
    /**
     * Get a string value from SharedPreferences
     */
    public String getString(String key, String defaultValue) {
        try {
            return sharedPreferences.getString(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error getting string: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * Save a boolean value to SharedPreferences
     */
    public void saveBoolean(String key, boolean value) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, value);
            editor.apply();
            Log.d(TAG, "Saved boolean: " + key + " = " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error saving boolean: " + key, e);
        }
    }
    
    /**
     * Get a boolean value from SharedPreferences
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return sharedPreferences.getBoolean(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error getting boolean: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * Save an integer value to SharedPreferences
     */
    public void saveInt(String key, int value) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
            Log.d(TAG, "Saved int: " + key + " = " + value);
        } catch (Exception e) {
            Log.e(TAG, "Error saving int: " + key, e);
        }
    }
    
    /**
     * Get an integer value from SharedPreferences
     */
    public int getInt(String key, int defaultValue) {
        try {
            return sharedPreferences.getInt(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Error getting int: " + key, e);
            return defaultValue;
        }
    }
    
    /**
     * Save an object as JSON to SharedPreferences
     */
    public <T> void saveObject(String key, T object) {
        try {
            String json = gson.toJson(object);
            saveString(key, json);
            Log.d(TAG, "Saved object: " + key + " as JSON");
        } catch (Exception e) {
            Log.e(TAG, "Error saving object: " + key, e);
        }
    }
    
    /**
     * Get an object from JSON stored in SharedPreferences
     */
    public <T> T getObject(String key, Class<T> classOfT, T defaultValue) {
        try {
            String json = getString(key, null);
            if (json != null) {
                return gson.fromJson(json, classOfT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting object: " + key, e);
        }
        return defaultValue;
    }
    
    /**
     * Save a map to SharedPreferences as JSON
     */
    public <K, V> void saveMap(String key, Map<K, V> map) {
        try {
            String json = gson.toJson(map);
            saveString(key, json);
            Log.d(TAG, "Saved map: " + key + " with " + map.size() + " entries");
        } catch (Exception e) {
            Log.e(TAG, "Error saving map: " + key, e);
        }
    }
    
    /**
     * Get a map from JSON stored in SharedPreferences
     */
    public <K, V> Map<K, V> getMap(String key, Class<K> keyClass, Class<V> valueClass) {
        try {
            String json = getString(key, null);
            if (json != null) {
                Type type = new TypeToken<HashMap<K, V>>(){}.getType();
                return gson.fromJson(json, type);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting map: " + key, e);
        }
        return new HashMap<>();
    }
    
    /**
     * Remove a key from SharedPreferences
     */
    public void remove(String key) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(key);
            editor.apply();
            Log.d(TAG, "Removed key: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Error removing key: " + key, e);
        }
    }
    
    /**
     * Clear all SharedPreferences data
     */
    public void clearAll() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Log.d(TAG, "Cleared all preferences");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing preferences", e);
        }
    }
    
    /**
     * Check if a key exists in SharedPreferences
     */
    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }
    
    /**
     * Get all keys from SharedPreferences
     */
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}
