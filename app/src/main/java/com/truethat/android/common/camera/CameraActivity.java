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
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import com.google.common.base.Supplier;
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

public abstract class CameraActivity extends AppCompatActivity {

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
  protected String TAG = "CameraActivity";
  // textureView should be assigned per Activity.
  protected TextureView mCameraPreview = null;
  private Image mLastTakenImage;
  /**
   * The listener is activated after an image is ready from {@link #takePicture()}.
   */
  private ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
    @Override public void onImageAvailable(ImageReader reader) {
      Log.v(TAG, "Image available.");
      Image image = null;
      try {
        image = reader.acquireLatestImage();
        mLastTakenImage = image;
        processImage();
      } finally {
        if (image != null) {
          image.close();
        }
      }
    }
  };
  /**
   * Supplies images for {@link #processImage()}. We use a supplier to gain outside control of the taken images for
   * testing purposes.
   */
  private Supplier<Image> mImageSupplier = new Supplier<Image>() {
    @Override public Image get() {
      return mLastTakenImage;
    }
  };
  private Size mImageDimension;
  private final TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
    @Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
      openCamera();
    }

    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
      return false;
    }

    @Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
  };
  private ImageReader mImageReader;
  private CameraDevice mCameraDevice;
  private CameraCaptureSession mCameraCaptureSessions;
  private CaptureRequest.Builder mCaptureRequestBuilder;
  private Handler mBackgroundHandler;
  private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
    @Override public void onOpened(@NonNull CameraDevice camera) {
      Log.v(TAG, "Camera opened.");
      mCameraDevice = camera;
      createCameraPreview();
    }

    @Override public void onDisconnected(@NonNull CameraDevice camera) {
      mCameraDevice.close();
    }

    @Override public void onError(@NonNull CameraDevice camera, int error) {
      mCameraDevice.close();
      mCameraDevice = null;
    }
  };
  private HandlerThread mBackgroundThread;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    TAG = this.getClass().getSimpleName();
    super.onCreate(savedInstanceState);
    Log.v(TAG, "Launching activity");
  }

  public Image supplyImage() {
    return mImageSupplier.get();
  }

  @VisibleForTesting public CameraDevice getCameraDevice() {
    return mCameraDevice;
  }

  @VisibleForTesting public void setImageSupplier(Supplier<Image> imageSupplier) {
    mImageSupplier = imageSupplier;
  }

  @VisibleForTesting
  public void setImageAvailableListener(ImageReader.OnImageAvailableListener imageAvailableListener) {
    mImageAvailableListener = imageAvailableListener;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

  /**
   * Takes a picture. Once the image is available, it is taken care of by {@code mImageAvailableListener}.
   */
  @SuppressWarnings("unused") protected void takePicture() {
    if (mCameraDevice == null) {
      Log.e(TAG, "Camera device is null.");
      return;
    }
    CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());
      @SuppressWarnings("ConstantConditions") Size[] jpegSizes =
          characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
      int width = 640;
      int height = 480;
      if (jpegSizes != null && 0 < jpegSizes.length) {
        width = jpegSizes[0].getWidth();
        height = jpegSizes[0].getHeight();
      }
      mImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
      List<Surface> outputSurfaces = new ArrayList<>(2);
      outputSurfaces.add(mImageReader.getSurface());
      if (mCameraPreview != null) {
        outputSurfaces.add(new Surface(mCameraPreview.getSurfaceTexture()));
      }
      final CaptureRequest.Builder captureBuilder =
          mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
      captureBuilder.addTarget(mImageReader.getSurface());
      captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      // Orientation
      int rotation = getWindowManager().getDefaultDisplay().getRotation();
      captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
      mImageReader.setOnImageAvailableListener(mImageAvailableListener, mBackgroundHandler);
      final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
        @Override public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
            @NonNull TotalCaptureResult result) {
          super.onCaptureCompleted(session, request, result);
          createCameraPreview();
        }
      };
      mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
        @Override public void onConfigured(@NonNull CameraCaptureSession session) {
          try {
            session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
          } catch (CameraAccessException e) {
            e.printStackTrace();
          }
        }

        @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
      }, mBackgroundHandler);
    } catch (NullPointerException | CameraAccessException e) {
      e.printStackTrace();
    }
  }

  protected void createCameraPreview() {
    try {
      SurfaceTexture texture = mCameraPreview == null ? new SurfaceTexture(10) : mCameraPreview.getSurfaceTexture();
      assert texture != null;
      texture.setDefaultBufferSize(mImageDimension.getWidth(), mImageDimension.getHeight());
      Surface surface = new Surface(texture);
      mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      mCaptureRequestBuilder.addTarget(surface);
      mCameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
        @Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
          //The camera is already closed
          if (null == mCameraDevice) {
            return;
          }
          // When the session is ready, we init displaying the preview.
          mCameraCaptureSessions = cameraCaptureSession;
          updatePreview();
        }

        @Override public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
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
      Log.e(TAG, "Camera device is null.");
    }
    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    try {
      mCameraCaptureSessions.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    Log.v(TAG, "Camera resumed.");
    startBackgroundThread();
    if (mCameraPreview == null || mCameraPreview.isAvailable()) {
      openCamera();
    } else {
      mCameraPreview.setSurfaceTextureListener(mTextureListener);
    }
  }

  @Override protected void onPause() {
    Log.e(TAG, "Camera paused.");
    closeCamera();
    stopBackgroundThread();
    super.onPause();
  }

  /**
   * Called automatically once an image is available from {@link #takePicture()}.
   * <p>
   * The image supplier should be used to obtain the last taken image.
   */
  protected abstract void processImage();

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
      StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
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
