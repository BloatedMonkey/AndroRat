package com.security.test.rat;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.security.test.rat.safe.SafeModeManager;

/**
 * Main Activity for the Android RAT application
 * This is the entry point and main interface
 */
public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private TextView connectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UI components
        initializeViews();
        setupToolbar();
        
        // Check if running in safe mode
        if (SafeModeManager.isSafeMode()) {
            showSafeModeMessage();
        } else {
            initializeNormalMode();
        }
    }

    private void initializeViews() {
        statusText = findViewById(R.id.status_text);
        connectionStatus = findViewById(R.id.connection_status);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Android RAT");
        }
    }

    private void showSafeModeMessage() {
        statusText.setText("Safe Mode Active - All features disabled");
        connectionStatus.setText("Status: Safe Mode");
        statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
    }

    private void initializeNormalMode() {
        statusText.setText("System Ready");
        connectionStatus.setText("Status: Initializing...");
        
        // Initialize core services
        initializeServices();
    }

    private void initializeServices() {
        // Initialize core modules
        try {
            // Start background services
            startBackgroundServices();
            connectionStatus.setText("Status: Connected");
        } catch (Exception e) {
            connectionStatus.setText("Status: Error - " + e.getMessage());
        }
    }

    private void startBackgroundServices() {
        // Start core background services
        // This would initialize the RAT functionality
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            // Open settings
            return true;
        } else if (id == R.id.action_about) {
            // Show about dialog
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update status when returning to app
        updateStatus();
    }

    private void updateStatus() {
        if (SafeModeManager.isSafeMode()) {
            return; // Don't update in safe mode
        }
        
        // Update connection status
        // This would check actual connection status
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cleanup resources
    }
}
