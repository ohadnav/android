package com.truethat.android.common.camera;

import android.content.Context;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import com.google.common.base.Supplier;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.BaseActivity;
import com.truethat.android.common.util.BackgroundHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 * <p>
 * Big thanks to https://inducesmile.com/android/android-camera2-api-example-tutorial/
 */

public abstract class CameraActivity extends BaseActivity {

  // Device possible orientations, so that pictures taken are rotated appropriately.
  private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

  // Initializes Orientations.
  static {
    ORIENTATIONS.append(Surface.ROTATION_0, 90);
    ORIENTATIONS.append(Surface.ROTATION_90, 0);
    ORIENTATIONS.append(Surface.ROTATION_180, 270);
    ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

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
  /**
   * Whether a picture should be taken as soon as the camera is opened. Useful for cases where {@link #takePicture()} is
   * invoked before the camera could be opened.
   */
  private boolean mTakePictureOnOpened = false;
  private Size mImageDimension;
  private final TextureView.SurfaceTextureListener mTextureListener =
      new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
          openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
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
  private BackgroundHandler mBackgroundHandler =
      new BackgroundHandler(this.getClass().getSimpleName());
  private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
    @Override public void onOpened(@NonNull CameraDevice camera) {
      Log.v(TAG, "Camera opened.");
      mCameraDevice = camera;
      if (mCameraPreview != null) createCameraPreview();
      // A handler is used since pictures cannot be taken immediately.
      if (mTakePictureOnOpened) {
        mBackgroundHandler.start();
        mBackgroundHandler.getHandler().postDelayed(new Runnable() {
          @Override public void run() {
            if (mTakePictureOnOpened) takePicture();
          }
        }, 100);
      }
    }

    @Override public void onDisconnected(@NonNull CameraDevice camera) {
      mCameraDevice.close();
    }

    @Override public void onError(@NonNull CameraDevice camera, int error) {
      mCameraDevice.close();
      mCameraDevice = null;
    }
  };

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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

  /**
   * Takes a picture. Once the image is available, it is taken care of by {@code mImageAvailableListener}.
   */
  @SuppressWarnings("unused") protected void takePicture() {
    // Resets onOpen take picture trigger.
    mTakePictureOnOpened = false;
    if (mCameraDevice == null) {
      Log.w(TAG,
          "Could not take a picture, since camera device was not opened yet. Will try to take one as soon as camera is opened.");
      mTakePictureOnOpened = true;
      return;
    }
    Log.v(TAG, "Taking picture.");
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
      final CaptureRequest.Builder captureBuilder =
          mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
      captureBuilder.addTarget(mImageReader.getSurface());
      captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      // Orientation
      int rotation = getWindowManager().getDefaultDisplay().getRotation();
      captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
      mImageReader.setOnImageAvailableListener(mImageAvailableListener,
          mBackgroundHandler.getHandler());
      final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
        @Override public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
            @NonNull TotalCaptureResult result) {
          super.onCaptureCompleted(session, request, result);
          if (mCameraPreview != null) createCameraPreview();
        }
      };
      mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
        @Override public void onConfigured(@NonNull CameraCaptureSession session) {
          try {
            session.capture(captureBuilder.build(), captureListener,
                mBackgroundHandler.getHandler());
          } catch (CameraAccessException e) {
            e.printStackTrace();
          }
        }

        @Override public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
      }, mBackgroundHandler.getHandler());
    } catch (NullPointerException | CameraAccessException e) {
      e.printStackTrace();
    }
  }

  private void createCameraPreview() {
    try {
      SurfaceTexture texture = mCameraPreview.getSurfaceTexture();
      texture.setDefaultBufferSize(mImageDimension.getWidth(), mImageDimension.getHeight());
      Surface surface = new Surface(texture);
      mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      mCaptureRequestBuilder.addTarget(surface);
      mCameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
        @Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
          //The camera is already closed
          if (null == mCameraDevice) {
            Log.w(TAG, "Did not update capture session, since camera is already closed.");
          } else {
            // When the session is ready, we init displaying the preview.
            mCameraCaptureSessions = cameraCaptureSession;
            updatePreview();
          }
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

  @Override protected void onResume() {
    super.onResume();
    Log.v(TAG, "Camera resumed.");
    mBackgroundHandler.start();
    if (mCameraPreview == null || mCameraPreview.isAvailable()) {
      openCamera();
    } else {
      mCameraPreview.setSurfaceTextureListener(mTextureListener);
    }
  }

  @Override protected void onPause() {
    Log.e(TAG, "Camera paused.");
    closeCamera();
    mBackgroundHandler.stop();
    super.onPause();
  }

  /**
   * Called automatically once an image is available from {@link #takePicture()}.
   * <p>
   * The image supplier should be used to obtain the last taken image.
   */
  protected abstract void processImage();

  private void updatePreview() {
    if (mCameraDevice == null) {
      Log.e(TAG, "Camera device is null when updating preview.");
    }
    mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    try {
      mCameraCaptureSessions.setRepeatingRequest(mCaptureRequestBuilder.build(), null,
          mBackgroundHandler.getHandler());
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
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
      if (cameraId == null) {
        throw new AssertionError("No you didn't! cameraId is null.. How dare you?!");
      }
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
      StreamConfigurationMap configurationMap =
          characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if (configurationMap == null) {
        throw new AssertionError("I am lost... configuration map is null.");
      }
      mImageDimension = configurationMap.getOutputSizes(SurfaceTexture.class)[0];
      App.getPermissionsModule().requestIfNeeded(this, Permission.CAMERA);
      // Does not open camera if no permission is granted
      if (!App.getPermissionsModule().isPermissionGranted(this, Permission.CAMERA)) {
        return;
      }
      Log.v(TAG, "Front camera opened.");
      manager.openCamera(cameraId, mStateCallback, null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "Could not open front camera.");
      e.printStackTrace();
    } catch (SecurityException e) {
      Log.e(TAG, "Camera permissions not granted, somebody has something to hide...");
      e.printStackTrace();
    }
  }
}
