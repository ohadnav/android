package com.truethat.android.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.media.Image;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.R;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.model.Edge;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Media;
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
  public final ObservableBoolean mPreviousMediaVisibility = new ObservableBoolean();
  public final ObservableBoolean mCancelButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSwitchCameraButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSendButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean();
  public final ObservableBoolean mScenePreviewVisibility = new ObservableBoolean();
  public final ObservableBoolean mCameraPreviewVisibility = new ObservableBoolean();
  public final ObservableInt mCaptureButtonDrawableResource = new ObservableInt(CAPTURE_RESOURCE);
  private DirectingState mState = DirectingState.CAMERA;
  private Scene mDirectedScene;
  private Media mCurrentMedia;
  private Media mNewMedia;
  private Edge mNewEdge;
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
            + call.request().url()
            + "\nDirected scene: "
            + mDirectedScene
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

  public Media getCurrentMedia() {
    return mCurrentMedia;
  }

  @Override public void onStop() {
    super.onStop();
    cancelSent();
  }

  @Override public void onStart() {
    super.onStart();
    switch (mState) {
      case EDIT:
        onEdit();
        break;
      case SENT:
        cancelSent();
        break;
      case PUBLISHED:
      case CAMERA:
      default:
        onCamera();
    }
    // Set default capture button
    mCaptureButtonDrawableResource.set(CAPTURE_RESOURCE);
  }

  @Override public void onPhotoTaken(Image image) {
    Log.d(TAG, "Photo taken.");
    mNewMedia = new Photo(null, CameraUtil.toByteArray(image));
    onEdit();
  }

  @Override public void onVideoRecorded(String videoPath) {
    Log.d(TAG, "Video recorded.");
    mCaptureButtonDrawableResource.set(CAPTURE_RESOURCE);
    mNewMedia = new Video(null, videoPath);
    onEdit();
  }

  @Override public void onVideoRecordStart() {
    mCaptureButtonDrawableResource.set(RECORD_RESOURCE);
  }

  /**
   * Go back to edit the previous media, after which the user can reach the current one.
   */
  public void previousMedia() {
    // Should reach here only if the current media node has a parent.
    mCurrentMedia =
        getDirectedScene().getFlowTree().getNodes().get(mCurrentMedia).getParent().getMedia();
    onEdit();
  }

  public void onReactionChosen(Emotion reaction) {
    if (mDirectedScene.getNextMedia(mCurrentMedia, reaction) != null) {
      mCurrentMedia = mDirectedScene.getNextMedia(mCurrentMedia, reaction);
      onEdit();
    } else {
      mNewEdge = new Edge(mDirectedScene.getMediaNodes().indexOf(mCurrentMedia), reaction);
      onCamera();
    }
  }

  public void onSent() {
    Log.d(TAG, "Change state: " + DirectingState.SENT.name());
    mState = DirectingState.SENT;
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

  /**
   * Cancel the current media, so that you will not be judged for eternal embarrassment.
   */
  public void disapprove() {
    Log.d(TAG, "Scene disapproved.");
    Media previous = mCurrentMedia;
    if (mDirectedScene.getFlowTree().getNodes().get(mCurrentMedia).getParent() != null) {
      mCurrentMedia =
          mDirectedScene.getFlowTree().getNodes().get(mCurrentMedia).getParent().getMedia();
      mDirectedScene.removeMedia(previous);
      onEdit();
    } else {
      mCurrentMedia = null;
      mDirectedScene = null;
      onCamera();
    }
  }

  public Scene getDirectedScene() {
    return mDirectedScene;
  }

  @VisibleForTesting public DirectingState getState() {
    return mState;
  }

  Edge getNewEdge() {
    return mNewEdge;
  }

  private void onEdit() {
    mState = DirectingState.EDIT;
    Log.d(TAG, "Change state: " + mState.name());
    if (mNewMedia == null) {
      if (mDirectedScene == null) {
        Log.w(TAG, "Trying to edit without a media to edit.");
        // No scene has been directed and there isn't any media to create one from, and so return
        // camera mode.
        onCamera();
        return;
      } else if (mCurrentMedia == null) {
        Log.w(TAG, "Editing with a null new and current media.");
        mCurrentMedia = mDirectedScene.getRootMediaNode();
      }
    } else {
      // Add media to directed scene
      if (mDirectedScene == null) {
        // No existing scene, so create one.
        mDirectedScene = new Scene(mNewMedia);
        mCurrentMedia = mNewMedia;
      } else {
        // Add media to flow tree
        if (mNewEdge != null) {
          mDirectedScene.addMedia(mNewMedia, mNewEdge);
          mCurrentMedia = mNewMedia;
          mNewEdge = null;
        } else {
          Log.w(TAG, "Editing scene flow without a chosen reaction.");
        }
      }
      mNewMedia = null;
    }
    // Exposes approval buttons.
    mCancelButtonVisibility.set(true);
    mSendButtonVisibility.set(true);
    // Expose previous media button if not editing root media.
    mPreviousMediaVisibility.set(!mCurrentMedia.equals(mDirectedScene.getRootMediaNode()));
    // Hides capture button.
    mCaptureButtonVisibility.set(false);
    mSwitchCameraButtonVisibility.set(false);
    // Hides loading image.
    mLoadingImageVisibility.set(false);
    // Shows the directed scene preview, and hides the camera preview.
    mScenePreviewVisibility.set(true);
    mCameraPreviewVisibility.set(false);
    getView().displayPreview(mCurrentMedia);
  }

  private void onCamera() {
    Log.d(TAG, "Change state: " + DirectingState.CAMERA.name());
    mState = DirectingState.CAMERA;
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
    getView().restoreCameraPreview();
    // Ensures scene state
    if (mDirectedScene != null && mNewEdge == null) {
      mCurrentMedia = getDirectedScene().getRootMediaNode();
      onEdit();
    }
  }

  private void cancelSent() {
    if (mSaveSceneCall != null) {
      mSaveSceneCall.cancel();
    }
    if (mState == DirectingState.SENT) {
      onEdit();
    }
  }

  private void onPublished() {
    Log.d(TAG, "Change state: " + DirectingState.PUBLISHED.name());
    mState = DirectingState.PUBLISHED;
    getView().toast(getContext().getString(R.string.saved_successfully));
    getView().leaveStudio();
  }

  private void onPublishError() {
    getView().toast(getContext().getString(R.string.sent_failed));
    onEdit();
  }

  @VisibleForTesting public enum DirectingState {
    /**
     * The user directs the piece of art he is about to create, using the camera.
     */
    CAMERA, /**
     * A {@link Scene} is edit and awaiting final approval to be published.
     */
    EDIT, /**
     * A {@link Scene} was approved and is now being sent to the server.
     */
    SENT, /**
     * The created {@link Scene} was successfully received by the server.
     */
    PUBLISHED
  }
}
