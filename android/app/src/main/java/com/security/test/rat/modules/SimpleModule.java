package com.security.test.rat.modules;

import android.content.Context;
import org.json.JSONObject;

/**
 * Simple test module for minimal build
 */
public class SimpleModule extends RATModule {
    
    public SimpleModule() {
        super();
    }
    
    @Override
    public String getModuleName() {
        return "SimpleModule";
    }
    
    @Override
    public String getModuleVersion() {
        return "1.0.0";
    }
    
    @Override
    public void executeCommand(JSONObject command) {
        // Simple command execution
        System.out.println("Executing command: " + command.toString());
    }
}
