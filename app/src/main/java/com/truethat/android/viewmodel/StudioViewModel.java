package com.truethat.android.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.R;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
import com.truethat.android.model.Video;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public class StudioViewModel extends BaseViewModel<StudioViewInterface>
    implements CameraFragment.CameraFragmentListener {
  @DrawableRes static final int CAPTURE_RESOURCE = R.drawable.capture;
  @DrawableRes static final int RECORD_RESOURCE = R.drawable.record;
  public final ObservableBoolean mCaptureButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mCancelButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSwitchCameraButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSendButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean();
  public final ObservableBoolean mScenePreviewVisibility = new ObservableBoolean();
  public final ObservableBoolean mCameraPreviewVisibility = new ObservableBoolean();
  public final ObservableInt mCaptureButtonDrawableResource = new ObservableInt(CAPTURE_RESOURCE);
  private DirectingState mDirectingState = DirectingState.DIRECTING;
  private Scene mDirectedScene;
  /**
   * Api call to save {@link #mDirectedScene}.
   */
  private Call<Scene> mSaveSceneCall;
  /**
   * Callback for {@link #mSaveSceneCall}.
   */
  private Callback<Scene> mSaveSceneCallback = new Callback<Scene>() {
    @Override public void onResponse(@NonNull Call<Scene> call, @NonNull Response<Scene> response) {
      if (response.isSuccessful() && response.body() != null) {
        mDirectedScene = response.body();
        onPublished();
      } else {
        if (!BuildConfig.DEBUG) {
          Crashlytics.logException(new Exception("Failed to save scene."));
        }
        Log.e(TAG, "Failed to save scene.\n"
            + call.request().url() + "\nDirected scene: " + mDirectedScene
            + "\nResponse: "
            + response.code()
            + " "
            + response.message()
            + "\n"
            + response.headers());
        onPublishError();
      }
    }

    @Override public void onFailure(@NonNull Call<Scene> call, @NonNull Throwable t) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(t);
      }
      t.printStackTrace();
      Log.e(TAG, "Saving scene request to " + call.request().url() + " had failed.", t);
      onPublishError();
    }
  };

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
  }

  @Override public void onStop() {
    super.onStop();
    cancelSent();
  }

  @Override public void onStart() {
    super.onStart();
    switch (mDirectingState) {
      case APPROVAL:
        onApproval();
        break;
      case SENT:
        cancelSent();
        break;
      case PUBLISHED:
      case DIRECTING:
      default:
        onDirecting();
    }
    // Set default capture button
    mCaptureButtonDrawableResource.set(CAPTURE_RESOURCE);
  }

  @Override public void onImageAvailable(Image image) {
    mDirectedScene = new Scene(new Photo(null, CameraUtil.toByteArray(image)));
    onApproval();
  }

  @Override public void onVideoAvailable(String videoPath) {
    mCaptureButtonDrawableResource.set(CAPTURE_RESOURCE);
    mDirectedScene = new Scene(new Video(null, videoPath));
    onApproval();
  }

  @Override public void onVideoRecordStart() {
    mCaptureButtonDrawableResource.set(RECORD_RESOURCE);
  }

  public void onSent() {
    Log.d(TAG, "Change state: " + DirectingState.SENT.name());
    mDirectingState = DirectingState.SENT;
    mSaveSceneCall = mDirectedScene.createApiCall();
    mSaveSceneCall.enqueue(mSaveSceneCallback);
    if (!BuildConfig.DEBUG) {
      Crashlytics.setString(LoggingKey.DIRECTED_SCENE.name(), mDirectedScene.toString());
    }
    // Hides buttons.
    mCaptureButtonVisibility.set(false);
    mSwitchCameraButtonVisibility.set(false);
    mCancelButtonVisibility.set(false);
    mSendButtonVisibility.set(false);
    mLoadingImageVisibility.set(true);
  }

  public void disapprove() {
    Log.d(TAG, "Scene disapproved.");
    onDirecting();
  }

  public Scene getDirectedScene() {
    return mDirectedScene;
  }

  @VisibleForTesting DirectingState getDirectingState() {
    return mDirectingState;
  }

  private void onApproval() {
    if (mDirectedScene == null) {
      onDirecting();
    }
    Log.d(TAG, "Change state: " + DirectingState.APPROVAL.name());
    mDirectingState = DirectingState.APPROVAL;
    // Exposes approval buttons.
    mCancelButtonVisibility.set(true);
    mSendButtonVisibility.set(true);
    // Hides capture button.
    mCaptureButtonVisibility.set(false);
    mSwitchCameraButtonVisibility.set(false);
    // Hides loading image.
    mLoadingImageVisibility.set(false);
    // Shows the directed scene preview, and hides the camera preview.
    mScenePreviewVisibility.set(true);
    mCameraPreviewVisibility.set(false);
    getView().displayPreview(mDirectedScene);
  }

  private void onDirecting() {
    Log.d(TAG, "Change state: " + DirectingState.DIRECTING.name());
    mDirectingState = DirectingState.DIRECTING;
    // Hides approval buttons.
    mCancelButtonVisibility.set(false);
    mSendButtonVisibility.set(false);
    // Exposes capture buttons.
    mCaptureButtonVisibility.set(true);
    mSwitchCameraButtonVisibility.set(true);
    // Hides loading image.
    mLoadingImageVisibility.set(false);
    // Hides the directed scene preview, and exposes the camera preview.
    mScenePreviewVisibility.set(false);
    mCameraPreviewVisibility.set(true);
    // Delete scene.
    mDirectedScene = null;
    getView().restoreCameraPreview();
  }

  private void cancelSent() {
    if (mSaveSceneCall != null) {
      mSaveSceneCall.cancel();
    }
    if (mDirectingState == DirectingState.SENT) {
      onApproval();
    }
  }

  private void onPublished() {
    Log.d(TAG, "Change state: " + DirectingState.PUBLISHED.name());
    mDirectingState = DirectingState.PUBLISHED;
    getView().toast(getContext().getString(R.string.saved_successfully));
    getView().leaveStudio();
  }

  private void onPublishError() {
    getView().toast(getContext().getString(R.string.sent_failed));
    onApproval();
  }

  @VisibleForTesting enum DirectingState {
    /**
     * The user directs the piece of art he is about to create, usually involves the camera preview.
     */
    DIRECTING, /**
     * A {@link Scene} was created and is awaiting final approval to be published.
     */
    APPROVAL, /**
     * A {@link Scene} was approved and is now being sent to the server.
     */
    SENT, /**
     * The created {@link Scene} was successfully received by the server.
     */
    PUBLISHED
  }
}
