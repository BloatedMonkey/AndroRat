package com.security.test.rat.modules;

import android.content.Context;
import org.json.JSONObject;

/**
 * Base class for all RAT modules
 * This is a stub implementation for build compatibility
 */
public abstract class RATModule {
    
    protected Context context;
    protected boolean isEnabled = false;
    
    public RATModule() {
        // Default constructor
    }
    
    public void initialize(Context context) {
        this.context = context;
        this.isEnabled = true;
    }
    
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
    
    public abstract String getModuleName();
    public abstract String getModuleVersion();
    
    public void executeCommand(JSONObject command) {
        // Default implementation - override in subclasses
    }
    
    public void cleanup() {
        // Default implementation - override in subclasses
    }
    
    public JSONObject getStatus() {
        // Default implementation - override in subclasses
        try {
            JSONObject status = new JSONObject();
            status.put("moduleName", getModuleName());
            status.put("moduleVersion", getModuleVersion());
            status.put("enabled", isEnabled);
            return status;
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
