package com.truethat.android.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;
import butterknife.BindView;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.util.AppUtil;
import com.truethat.android.common.util.BackgroundHandler;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.databinding.FragmentCameraBinding;
import com.truethat.android.view.custom.FullscreenTextureView;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import com.truethat.android.viewmodel.viewinterface.BaseListener;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static com.truethat.android.common.util.AppUtil.isEmulator;
import static com.truethat.android.common.util.CameraUtil.SIZE_AREA_COMPARATOR;
import static com.truethat.android.common.util.CameraUtil.chooseVideoSize;

/**
 * Taken from Google example at <a>https://github.com/googlesamples/android-Camera2Basic/blob/master/Application/src/main/java/com/example/android/camera2basic/Camera2BasicFragment.java</a>
 */
public class CameraFragment extends
    BaseFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, FragmentCameraBinding>
    implements BaseViewInterface {
  public static final String FRAGMENT_TAG = CameraFragment.class.getSimpleName();
  /**
   * Whether selfie camera or back camera should be used, as per {@link CameraUtil.Facing}.
   */
  private static final String ARG_FACING = "facing";
  /**
   * Conversion from screen rotation to captured media orientation (applies for both still photos
   * and videos).
   */
  private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();

  /**
   * Inverse conversion from screen rotation to captured media orientation for rotated devices
   */
  private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

  private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
  private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;

  /**
   * Max preview size that is guaranteed by Camera2 API
   */
  private static final Size MAX_SIZE = new Size(1920, 1080);
  private static final int MAX_VIDEO_MS = 10 * 1000;

  static {
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
    DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
  }

  static {
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
    INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
  }

  /**
   * A {@link FullscreenTextureView} for camera preview.
   */
  @BindView(R.id.cameraPreview) FullscreenTextureView mCameraPreview;
  /**
   * Listener for taken pictures, videos and changed states.
   */
  private CameraFragmentListener mCameraFragmentListener;
  /**
   * This a callback object for the {@link ImageReader}. "onPhotoTaken" will be called when a
   * still image is ready to be processed.
   */
  private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
      new ImageReader.OnImageAvailableListener() {
        @Override public void onImageAvailable(ImageReader reader) {
          Image image = null;
          try {
            image = reader.acquireLatestImage();
            mCameraFragmentListener.onPhotoTaken(image);
          } finally {
            if (image != null) {
              image.close();
            }
          }
        }
      };
  /**
   * ID of the current {@link CameraDevice}.
   */
  private String mCameraId;
  /**
   * ID of the current {@link CameraDevice}.
   */
  private CameraUtil.Facing mFacing = CameraUtil.Facing.FRONT;
  /**
   * A reference to the opened {@link CameraDevice}.
   */
  private CameraDevice mCameraDevice;
  /**
   * An additional thread for running tasks that shouldn't block the UI.
   */
  private BackgroundHandler mBackgroundHandler;
  /**
   * An {@link ImageReader} that handles still image capture.
   */
  private ImageReader mImageReader;
  /**
   * The {@link Size} of camera preview.
   */
  private Size mPreviewSize;
  /**
   * A {@link CameraCaptureSession} for camera preview.
   */
  private CameraCaptureSession mPreviewSession;
  /**
   * {@link CaptureRequest.Builder} for the camera preview
   */
  private CaptureRequest.Builder mPreviewRequestBuilder;
  /**
   * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder} to display the camera
   * preview.
   */
  private CaptureRequest mPreviewRequest;

  /**
   * The {@link Size} of video recording.
   */
  private Size mVideoSize;

  /**
   * MediaRecorder
   */
  private MediaRecorder mMediaRecorder;

  /**
   * Absolute path to internal storage in which to store the next video.
   */
  private String mVideoAbsolutePath;
  /**
   * The {@link Size} of captured photos.
   */
  private Size mCaptureSize;
  /**
   * The current state of camera state for taking pictures.
   *
   * @see #mCaptureCallback
   */
  private CameraState mState = CameraState.PREVIEW;
  /**
   * A {@link Semaphore} to prevent the app from exiting before closing the camera.
   */
  private Semaphore mCameraOpenCloseLock = new Semaphore(1);
  /**
   * Whether the current camera device supports Flash or not.
   */
  private boolean mFlashSupported;
  /**
   * Orientation of the camera sensor
   */
  private int mSensorOrientation;
  /**
   * Available autofocus modes on the device's front camera. If the device does not have {@link
   * CaptureResult#CONTROL_AF_STATE_FOCUSED_LOCKED} and {@link CaptureResult#CONTROL_AF_STATE_NOT_FOCUSED_LOCKED},
   * then auto focus is skipped when taking pictures.
   */
  private List<Integer> mAutofocusAvailableModes;
  /**
   * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
   */
  private CameraCaptureSession.CaptureCallback mCaptureCallback =
      new CameraCaptureSession.CaptureCallback() {

        @Override public void onCaptureProgressed(@NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
          process(partialResult);
        }

        @Override public void onCaptureCompleted(@NonNull CameraCaptureSession session,
            @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
          process(result);
        }

        private void process(CaptureResult result) {
          switch (mState) {
            case PREVIEW: {
              // We have nothing to do when the camera preview is working normally.
              break;
            }
            case WAITING_LOCK: {
              Integer autofocusState = result.get(CaptureResult.CONTROL_AF_STATE);
              if (autofocusState == null) {
                captureStillPicture();
              } else if (!mAutofocusAvailableModes.containsAll(
                  Arrays.asList(CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED,
                      CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED))
                  || CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == autofocusState
                  || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == autofocusState) {
                // CONTROL_AE_STATE can be null on some devices
                Integer autoExposureState = result.get(CaptureResult.CONTROL_AE_STATE);
                if (autoExposureState == null
                    || autoExposureState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                  captureStillPicture();
                } else {
                  runPrecaptureSequence();
                }
              } else {
                Log.w(TAG, "Camera failed to focus.");
              }
              break;
            }
            case WAITING_PRECAPTURE: {
              // CONTROL_AE_STATE can be null on some devices
              Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
              if (aeState == null
                  || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE
                  || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                mState = CameraState.WAITING_FOR_CAPTURE;
              }
              break;
            }
            case WAITING_FOR_CAPTURE: {
              // CONTROL_AE_STATE can be null on some devices
              Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
              if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                mState = CameraState.PICTURE_TAKEN;
                captureStillPicture();
              }
              break;
            }
          }
        }
      };
  /**
   * Whether to rotate preview for emulator, so that it appears with the correct orientation.
   */
  private boolean mAdjustForEmulator = isEmulator();
  /**
   * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
   */
  private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

    @Override public void onOpened(@NonNull CameraDevice cameraDevice) {
      // This method is called when the camera is opened.
      mCameraOpenCloseLock.release();
      mCameraDevice = cameraDevice;
      if (mCameraPreview.getVisibility() != GONE) {
        // We start the camera preview here
        getActivity().runOnUiThread(new Runnable() {
          @Override public void run() {
            createCameraPreviewSession();
          }
        });
      }
    }

    @Override public void onDisconnected(@NonNull CameraDevice cameraDevice) {
      mCameraOpenCloseLock.release();
      cameraDevice.close();
      mCameraDevice = null;
    }

    @Override public void onError(@NonNull CameraDevice cameraDevice, int error) {
      mCameraOpenCloseLock.release();
      cameraDevice.close();
      mCameraDevice = null;
      Log.e(TAG, "Camera error " + error);
    }
  };
  /**
   * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
   * {@link TextureView}.
   */
  private final TextureView.SurfaceTextureListener mSurfaceTextureListener =
      new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
          openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
          configureTransform(width, height);
        }

        @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
          return true;
        }

        @Override public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }
      };

  public static CameraFragment newInstance() {
    CameraFragment fragment = new CameraFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  /**
   * Initiate a still image capture.
   */
  public void takePicture() {
    Log.d(TAG, "takePicture (sized " + mCaptureSize + ")");
    lockFocus();
  }

  /**
   * Start record the next Titanic!
   */
  public void startRecordVideo() {
    AppContainer.getPermissionsManager().requestIfNeeded(getActivity(), Permission.RECORD_AUDIO);
    if (!AppContainer.getPermissionsManager().isPermissionGranted(Permission.RECORD_AUDIO)) {
      Log.i(TAG, Permission.RECORD_AUDIO + " is not granted, not starting video recording.");
      return;
    }
    if (null == mCameraDevice || !mCameraPreview.isAvailable() || null == mPreviewSize) {
      Log.i(TAG, "Not ready to start recording a video.");
      return;
    }
    try {
      Log.d(TAG, "startRecordVideo (" + mVideoSize + ")");
      mState = CameraState.WAITING_RECORDING;
      closePreviewSession();
      setUpMediaRecorder();
      SurfaceTexture texture = mCameraPreview.getSurfaceTexture();
      if (texture == null) {
        throw new AssertionError("Texture is null, unable to record video.");
      }
      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
      List<Surface> surfaces = new ArrayList<>();

      // Set up Surface for the camera preview
      Surface previewSurface = new Surface(texture);
      surfaces.add(previewSurface);
      mPreviewRequestBuilder.addTarget(previewSurface);

      // Set up Surface for the MediaRecorder
      Surface recorderSurface = mMediaRecorder.getSurface();
      surfaces.add(recorderSurface);
      mPreviewRequestBuilder.addTarget(recorderSurface);

      // Start a capture session
      // Once the session starts, we can update the UI and start recording
      mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

        @Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
          mPreviewSession = cameraCaptureSession;
          preparePreviewForVideoRecording();
          getActivity().runOnUiThread(new Runnable() {
            @Override public void run() {
              Log.v(TAG, "video recording has started.");
              mCameraFragmentListener.onVideoRecordStart();
              // Start recording
              mMediaRecorder.start();
              mState = CameraState.RECORDING_VIDEO;
            }
          });
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
          Activity activity = getActivity();
          if (null != activity) {
            Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
          }
        }
      }, mBackgroundHandler.getHandler());
    } catch (CameraAccessException | IOException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    }
  }

  /**
   * And....cut!
   */
  public void stopRecordVideo() {
    // TODO(ohad): handle stop failures.
    if (!isRecordingVideo()) {
      Log.v(TAG, "Trying to stop video when not recording");
      return;
    }
    Log.d(TAG, "stopRecordVideo");
    // Stop recording
    mMediaRecorder.stop();
    mMediaRecorder.reset();
    mCameraFragmentListener.onVideoRecorded(mVideoAbsolutePath);
    mVideoAbsolutePath = null;
    mState = CameraState.VIDEO_RECORDED;
  }

  public boolean isRecordingVideo() {
    return mState == CameraState.RECORDING_VIDEO;
  }

  public void setCameraFragmentListener(CameraFragmentListener cameraFragmentListener) {
    mCameraFragmentListener = cameraFragmentListener;
  }

  /**
   * When the screen is turned off and turned back on, the SurfaceTexture is already available, and
   * "onSurfaceTextureAvailable" will not be called. In that case, we can open a camera and start
   * preview from here (otherwise, we wait until the surface is ready in the {@link
   * TextureView.SurfaceTextureListener}).
   */
  @Override public void onVisible() {
    super.onVisible();
    if (mBackgroundHandler == null) {
      mBackgroundHandler = new BackgroundHandler(this.getClass().getSimpleName());
    }
    mBackgroundHandler.start();
    if (!isCameraOpen()) {
      // Open camera
      if (mCameraPreview.isAvailable()) {
        openCamera();
      } else {
        mCameraPreview.setSurfaceTextureListener(mSurfaceTextureListener);
      }
    }
  }

  @Override public void onHidden() {
    super.onHidden();
    closeCamera();
    if (mBackgroundHandler != null) {
      mBackgroundHandler.stop();
    }
  }

  @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    if (savedInstanceState != null) {
      if (savedInstanceState.getSerializable(ARG_FACING) != null) {
        mFacing = (CameraUtil.Facing) savedInstanceState.getSerializable(ARG_FACING);
      } else {
        // Default camera is selfie camera.
        mFacing = CameraUtil.Facing.FRONT;
      }
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable(ARG_FACING, mFacing);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    closeCamera();
    if (mBackgroundHandler != null) {
      mBackgroundHandler.stop();
    }
  }

  @Override public void onDetach() {
    super.onDetach();
    mCameraFragmentListener = null;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_camera, getContext());
  }

  public FullscreenTextureView getCameraPreview() {
    return mCameraPreview;
  }

  public boolean isCameraOpen() {
    return mCameraDevice != null;
  }

  public CameraState getState() {
    return mState;
  }

  public CameraUtil.Facing getFacing() {
    return mFacing;
  }

  /**
   * Switches between {@link CameraUtil.Facing#FRONT} and {@link CameraUtil.Facing#BACK}
   */
  public void switchCamera() {
    Log.d(TAG, "switchCamera");
    // Close camera.
    closeCamera();
    // Switching cameras.
    mFacing = mFacing == CameraUtil.Facing.FRONT ? CameraUtil.Facing.BACK : CameraUtil.Facing.FRONT;
    // Reopens camera.
    openCamera();
  }

  /**
   * Restores camera preview after a picture was taken, or after a video was recorded.
   */
  public void restorePreview() {
    if (cameraNotPrepared() || mCameraPreview == null) {
      Log.d(TAG, "Could not restore preview because camera or its preview are not ready.");
      return;
    }
    Log.d(TAG, "restorePreview");
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        createCameraPreviewSession();
        mState = CameraState.PREVIEW;
      }
    });
  }

  /**
   * @return whether a photo can be taken at the current state.
   */
  public boolean cameraNotPrepared() {
    return getActivity() == null || !isCameraOpen() || !isResumed();
  }

  /**
   * Closes the camera preview session in favour of video recording.
   */
  private void closePreviewSession() {
    if (mPreviewSession != null) {
      mPreviewSession.close();
      mPreviewSession = null;
    }
  }

  /**
   * Prepares camera preview for video recording.
   */
  private void preparePreviewForVideoRecording() {
    if (null == mCameraDevice) {
      return;
    }
    try {
      mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      HandlerThread thread = new HandlerThread("CameraPreview");
      thread.start();
      mPreviewSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null,
          mBackgroundHandler.getHandler());
    } catch (CameraAccessException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    }
  }

  /**
   * Adjusts camera preview for emulator defects, so that it looks normal.
   */
  private void adjustForEmulator() {
    Matrix matrix = new Matrix();
    Point displaySize = AppUtil.realDisplaySize(getActivity());
    RectF viewRect = new RectF(0, 0, mCameraPreview.getWidth(), mCameraPreview.getHeight());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    float scale = (float) mCameraPreview.getWidth() / (float) displaySize.x;
    matrix.postScale(scale, scale, centerX, centerY);
    matrix.postRotate(270, centerX, centerY);
    mCameraPreview.setTransform(matrix);
  }

  /**
   * Sets up member variables related to camera.
   */
  private void setUpCameraOutputs() {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      mCameraId = CameraUtil.getCameraId(manager, mFacing);
      if (mCameraId == null) {
        throw new AssertionError("Front facing camera has a null ID, forgive me lord.");
      }
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);

      // Gathering autofocus modes.
      mAutofocusAvailableModes = new ArrayList<>();
      int[] availableAutofocusModes =
          characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
      if (availableAutofocusModes != null) {
        for (int autofocusMode : availableAutofocusModes) {
          mAutofocusAvailableModes.add(autofocusMode);
        }
      }

      StreamConfigurationMap map =
          characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if (map == null) {
        throw new AssertionError("OMG configuration map is null.");
      }
      // Set up video size
      Point displaySize = AppUtil.realDisplaySize(getActivity());
      // Inversing display size to match Media Recorder output sizes.
      mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class),
          displaySize.y / (double) displaySize.x);
      // For still image captures, we use the largest available size.
      mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
          SIZE_AREA_COMPARATOR);
      mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
          ImageFormat.JPEG, 2);
      // Check if the flash is supported.
      Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
      mFlashSupported = available == null ? false : available;
    } catch (CameraAccessException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    } catch (NullPointerException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      // Currently an NPE is thrown when the Camera2API is used but not supported on the
      // device this code runs.
      Log.e(TAG, "Camera2API is not supported.");
    }
  }

  @SuppressWarnings("SuspiciousNameCombination") private void setUpPreviewOutputs() {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
    try {
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
      StreamConfigurationMap map =
          characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if (map == null) {
        throw new AssertionError("OMG configuration map is null.");
      }
      // Find out if we need to swap dimension to get the preview size relative to sensor
      // coordinate.
      int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      //noinspection ConstantConditions
      mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
      boolean swappedDimensions = false;
      switch (displayRotation) {
        case Surface.ROTATION_0:
        case Surface.ROTATION_180:
          if (mSensorOrientation == 90 || mSensorOrientation == 270) {
            swappedDimensions = true;
          }
          break;
        case Surface.ROTATION_90:
        case Surface.ROTATION_270:
          if (mSensorOrientation == 0 || mSensorOrientation == 180) {
            swappedDimensions = true;
          }
          break;
        default:
          Log.e(TAG, "Display rotation is invalid: " + displayRotation);
      }

      // Using real display size to aim for full screen.
      Point displaySize = AppUtil.realDisplaySize(getActivity());
      int rotatedPreviewWidth = mCameraPreview.getWidth();
      int rotatedPreviewHeight = mCameraPreview.getHeight();
      int maxPreviewWidth = displaySize.x;
      int maxPreviewHeight = displaySize.y;

      if (swappedDimensions) {
        rotatedPreviewWidth = mCameraPreview.getHeight();
        rotatedPreviewHeight = mCameraPreview.getWidth();
        maxPreviewWidth = displaySize.y;
        maxPreviewHeight = displaySize.x;
      }

      if (maxPreviewWidth > MAX_SIZE.getWidth()) {
        maxPreviewWidth = MAX_SIZE.getWidth();
      }

      if (maxPreviewHeight > MAX_SIZE.getHeight()) {
        maxPreviewHeight = MAX_SIZE.getHeight();
      }

      // (1) Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
      // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
      // garbage capture data.
      // (2) We use real display size, so that the preview is fullscreen, if possible.
      mPreviewSize =
          CameraUtil.choosePhotoSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth,
              rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, displaySize);

      // We fit the aspect ratio of TextureView to the size of preview we picked.
      final int orientation = getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        mCameraPreview.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      } else {
        mCameraPreview.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
      }
    } catch (CameraAccessException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    }
  }

  /**
   * Opens the camera specified by {@link #mCameraId}.
   */
  @SuppressWarnings("MissingPermission") private void openCamera() {
    AppContainer.getPermissionsManager().requestIfNeeded(getActivity(), Permission.CAMERA);
    if (!AppContainer.getPermissionsManager().isPermissionGranted(Permission.CAMERA)) {
      return;
    }
    Log.d(TAG, "openCamera");
    mMediaRecorder = new MediaRecorder();
    setUpCameraOutputs();
    CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
    try {
      if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock camera opening.");
      }
      manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler.getHandler());
    } catch (CameraAccessException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    } catch (InterruptedException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
      throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
    }
  }

  /**
   * Closes the current {@link CameraDevice}.
   */
  private void closeCamera() {
    Log.d(TAG, "closeCamera");
    try {
      mCameraOpenCloseLock.acquire();
      if (null != mPreviewSession) {
        mPreviewSession.close();
        mPreviewSession = null;
      }
      if (null != mCameraDevice) {
        mCameraDevice.close();
        mCameraDevice = null;
      }
      if (null != mImageReader) {
        mImageReader.close();
        mImageReader = null;
      }
      if (null != mMediaRecorder) {
        mMediaRecorder.release();
        mMediaRecorder = null;
      }
    } catch (InterruptedException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      mCameraOpenCloseLock.release();
    }
  }

  /**
   * Prepares {@link #mMediaRecorder} for recording video.
   */
  private void setUpMediaRecorder() throws IOException {
    mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    if (mVideoAbsolutePath == null || mVideoAbsolutePath.isEmpty()) {
      mVideoAbsolutePath = getVideoFilePath();
    }
    mMediaRecorder.setOutputFile(mVideoAbsolutePath);
    mMediaRecorder.setVideoEncodingBitRate(5000000);
    mMediaRecorder.setVideoFrameRate(30);
    mMediaRecorder.setMaxDuration(MAX_VIDEO_MS);
    mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
    switch (mSensorOrientation) {
      case SENSOR_ORIENTATION_DEFAULT_DEGREES:
        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
        break;
      case SENSOR_ORIENTATION_INVERSE_DEGREES:
        mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
        break;
    }
    mMediaRecorder.prepare();
  }

  /**
   * Creates a new {@link CameraCaptureSession} for camera preview.
   */
  @MainThread private void createCameraPreviewSession() {
    Log.d(TAG, "createCameraPreviewSession");
    try {
      mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      setUpPreviewOutputs();
      configureTransform(mCameraPreview.getWidth(), mPreviewSize.getHeight());
      if (mAdjustForEmulator) {
        mBackgroundHandler.getHandler().postDelayed(new Runnable() {
          @Override public void run() {
            getActivity().runOnUiThread(new Runnable() {
              @Override public void run() {
                adjustForEmulator();
              }
            });
          }
        }, 200);
      }

      SurfaceTexture texture = mCameraPreview.getSurfaceTexture();
      if (texture == null) {
        throw new AssertionError("Preview texture is null");
      }
      // We configure the size of default buffer to be the size of camera preview we want.
      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

      // This is the output Surface we need to start preview.
      Surface surface = new Surface(texture);

      // We set up a CaptureRequest.Builder with the output Surface.
      mPreviewRequestBuilder.addTarget(surface);

      // Here, we create a CameraCaptureSession for camera preview.
      mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
          new CameraCaptureSession.StateCallback() {

            @Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              if (cameraNotPrepared()) {
                return;
              }

              // When the session is ready, we start displaying the preview.
              mPreviewSession = cameraCaptureSession;
              try {
                // Auto focus should be continuous for camera preview.
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                if (mState == CameraState.PREVIEW) {
                  // Finally, we start displaying the camera preview.
                  mPreviewRequest = mPreviewRequestBuilder.build();
                  mPreviewSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                      mBackgroundHandler.getHandler());
                }
              } catch (CameraAccessException e) {
                if (!BuildConfig.DEBUG) {
                  Crashlytics.logException(e);
                }
                e.printStackTrace();
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              Crashlytics.logException(new Exception("Camera configuration had failed."));
              Log.e(TAG, "Configuration failed.");
            }
          }, null);
    } catch (CameraAccessException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    }
  }

  /**
   * Configures the necessary {@link Matrix} transformation to {@link
   * #mCameraPreview}. This method should be called after the camera preview size is determined in
   * {@link #setUpCameraOutputs()} and also the size of {@link #mCameraPreview} is fixed.
   *
   * @param viewWidth  The width of {@link #mCameraPreview}
   * @param viewHeight The height of {@link #mCameraPreview}
   */
  private void configureTransform(int viewWidth, int viewHeight) {
    Activity activity = getActivity();
    if (null == mCameraPreview || null == mPreviewSize || null == activity) {
      throw new IllegalStateException("Camera preview is not ready");
    }
    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
    Matrix matrix = new Matrix();
    RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
    RectF bufferRect = new RectF(0, 0, mPreviewSize.getWidth(), mPreviewSize.getHeight());
    float centerX = viewRect.centerX();
    float centerY = viewRect.centerY();
    if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
      bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
      matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
      float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(),
          (float) viewWidth / mPreviewSize.getWidth());
      matrix.postScale(scale, scale, centerX, centerY);
      matrix.postRotate(90 * (rotation - 2), centerX, centerY);
    } else if (Surface.ROTATION_180 == rotation) {
      matrix.postRotate(180, centerX, centerY);
    }
    mCameraPreview.setTransform(matrix);
  }

  /**
   * Lock the focus as the first step for a still image capture.
   */
  private void lockFocus() {
    try {
      // This is how to tell the camera to lock focus.
      mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
          CameraMetadata.CONTROL_AF_TRIGGER_START);
      // Tell #mCaptureCallback to wait for the lock.
      mState = CameraState.WAITING_LOCK;
      mPreviewSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
          mBackgroundHandler.getHandler());
    } catch (Exception e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    }
  }

  /**
   * Run the precapture sequence for capturing a still image. This method should be called when
   * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
   */
  private void runPrecaptureSequence() {
    try {
      // This is how to tell the camera to trigger.
      mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
          CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
      // Tell #mCaptureCallback to wait for the precapture sequence to be set.
      mState = CameraState.WAITING_PRECAPTURE;
      mPreviewSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
          mBackgroundHandler.getHandler());
    } catch (CameraAccessException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    }
  }

  /**
   * Capture a still picture. This method should be called when we get a response in
   * {@link #mCaptureCallback} from {@link #lockFocus()}.
   */
  private void captureStillPicture() {
    try {
      final Activity activity = getActivity();
      if (cameraNotPrepared()) {
        Log.i(TAG, "Capturing still picture aborted, as view is no longer visible.");
        return;
      }
      // This is the CaptureRequest.Builder that we use to take a picture.
      final CaptureRequest.Builder captureBuilder =
          mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
      captureBuilder.addTarget(mImageReader.getSurface());

      // Sets the image available callback.
      mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
          mBackgroundHandler.getHandler());

      // Use the same AE and AF modes as the preview.
      captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
          CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
      setAutoFlash(captureBuilder);

      // Orientation
      int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));

      CameraCaptureSession.CaptureCallback captureCallback =
          new CameraCaptureSession.CaptureCallback() {

            @Override public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            }
          };

      if (mPreviewSession != null) {
        mPreviewSession.stopRepeating();
        mPreviewSession.capture(captureBuilder.build(), captureCallback, null);
      }
      mState = CameraState.PICTURE_TAKEN;
    } catch (CameraAccessException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
      e.printStackTrace();
    }
  }

  /**
   * Retrieves the JPEG orientation from the specified screen rotation.
   * <p>
   * Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X) We have to
   * take that into account and rotate JPEG properly. For devices with orientation of 90, we simply
   * return our mapping from {@link #DEFAULT_ORIENTATIONS}. For devices with orientation of 270, we
   * need to
   * rotate the JPEG 180 degrees.
   *
   * @param rotation The screen rotation.
   *
   * @return The JPEG orientation (one of 0, 90, 270, and 360)
   */
  private int getOrientation(int rotation) {

    return (DEFAULT_ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
  }

  private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
    if (mFlashSupported) {
      requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
          CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
    }
  }

  /**
   * @return the path in which to store the next video.
   */
  private String getVideoFilePath() {
    final File dir = getActivity().getFilesDir();
    return (dir == null ? "" : (dir.getAbsolutePath() + "/")) + System.currentTimeMillis() + ".mp4";
  }

  /**
   * Camera state when it comes to showing preview, taking pictures and the like.
   */
  public enum CameraState {
    /**
     * Showing camera preview.
     */
    PREVIEW, /**
     * Waiting for the focus to be locked.
     */
    WAITING_LOCK, /**
     * Waiting for the exposure to be precapture state.
     */
    WAITING_PRECAPTURE, /**
     * Waiting for the exposure state to be something other than precapture, usually a capture.
     */
    WAITING_FOR_CAPTURE, /**
     * Picture was taken, and preview had not been restored.
     */
    PICTURE_TAKEN, /**
     * Waiting for video recording to start
     */
    WAITING_RECORDING, /**
     * Currently recording video.
     */
    RECORDING_VIDEO, /**
     * Video was recorded, and preview has not been restored.
     */
    VIDEO_RECORDED
  }

  public interface CameraFragmentListener extends BaseListener {
    /**
     * Callback for taken pictures.
     *
     * @param image fresh from the oven image.
     */
    void onPhotoTaken(Image image);

    /**
     * Callback for ready videos. Users may be haunted with eternal fame.
     *
     * @param videoPath file path o internal storage where the video is stored.
     */
    void onVideoRecorded(String videoPath);

    /**
     * Called once video recording has started.
     */
    void onVideoRecordStart();
  }
}
