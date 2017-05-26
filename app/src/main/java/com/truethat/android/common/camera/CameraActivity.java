package com.truethat.android.common.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import com.truethat.android.application.App;
import com.truethat.android.application.permissions.Permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 * <p>
 * Big thanks to https://inducesmile.com/android/android-camera2-api-example-tutorial/
 */

public class CameraActivity extends AppCompatActivity {

    // Device possible orientations, so that pictures taken are rotated appropriately.
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    // Initializes Orientations.
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    // TAG should be updated per implementation.
    protected String      TAG            = "CameraActivity";
    // textureView should be assigned per Activity.
    protected TextureView mCameraPreview = null;
    private Image                  mLastTakenImage;
    private Size                   mImageDimension;
    private ImageReader            mImageReader;
    private CameraDevice           mCameraDevice;
    private CameraCaptureSession   mCameraCaptureSessions;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Handler                mBackgroundHandler;
    private final CameraDevice.StateCallback         mStateCallback   = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.v(TAG, "Camera opened.");
            mCameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mCameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    };
    private final TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private HandlerThread mBackgroundThread;

    @VisibleForTesting
    public Image getLastTakenImage() {
        return mLastTakenImage;
    }

    @VisibleForTesting
    public CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (!App.getPermissionsModule().isPermissionGranted(this, Permission.CAMERA)) {
            Log.w(TAG, "Camera permissions not granted.");
            onRequestPermissionsFailed();
        }
    }

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    protected void takePicture() {
        if (mCameraDevice == null) {
            Log.e(TAG, "camera device is null.");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager
                    .getCameraCharacteristics(mCameraDevice.getId());
            @SuppressWarnings("ConstantConditions")
            Size[] jpegSizes = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);
            int width  = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader
                    .newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            if (mCameraPreview != null) {
                outputSurfaces.add(new Surface(mCameraPreview.getSurfaceTexture()));
            }
            final CaptureRequest.Builder captureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            ImageReader.OnImageAvailableListener readerListener = availableListener -> {
                Image image = null;
                try {
                    image = availableListener.acquireLatestImage();
                    mLastTakenImage = image;
                    processImage(image);
                } finally {
                    if (image != null) {
                        image.close();
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    createCameraPreview();
                }
            };
            mCameraDevice
                    .createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.capture(captureBuilder.build(), captureListener,
                                                mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    }, mBackgroundHandler);
        } catch (NullPointerException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = mCameraPreview == null ?
                                     new SurfaceTexture(10) : mCameraPreview.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mImageDimension.getWidth(), mImageDimension.getHeight());
            Surface surface = new Surface(texture);
            mCaptureRequestBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            //The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }
                            // When the session is ready, we start displaying the preview.
                            mCameraCaptureSessions = cameraCaptureSession;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            Log.e(TAG, "Configuration failed.");
                        }
                    }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "No camera access.");
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        if (mCameraDevice == null) {
            Log.e(TAG, "camera device is null.");
        }
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            mCameraCaptureSessions
                    .setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "resumed");
        startBackgroundThread();
        if (mCameraPreview == null || mCameraPreview.isAvailable()) {
            openCamera();
        } else {
            mCameraPreview.setSurfaceTextureListener(mTextureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "paused.");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @SuppressWarnings("UnusedParameters")
    protected void processImage(Image image) {
    }

    protected void onRequestPermissionsFailed() {
        startActivity(new Intent(this, NoCameraPermissionActivity.class));
    }

    private void closeCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            final String cameraId = CameraUtil.getFrontFacingCameraId(manager);
            assert cameraId != null;
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            mImageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            App.getPermissionsModule().requestIfNeeded(this, Permission.CAMERA);
            // Does not open camera if no permission is granted
            if (!App.getPermissionsModule().isPermissionGranted(this, Permission.CAMERA)) {
                return;
            }
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Could not open front camera.");
            e.printStackTrace();
        } catch (SecurityException e) {
            Log.e(TAG, "Camera permissions not granted, somebody has something to hide...");
            e.printStackTrace();
        }
        Log.v(TAG, "Front camera opened.");
    }
}
