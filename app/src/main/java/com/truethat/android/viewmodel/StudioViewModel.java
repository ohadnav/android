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

public class StudioViewModel extends BaseFragmentViewModel<StudioViewInterface>
    implements CameraFragment.CameraFragmentListener {
  @DrawableRes static final int RECORD_RESOURCE = R.drawable.record;
  @DrawableRes static final int CAPTURE_RESOURCE = R.drawable.capture;
  private static final String BUNDLE_DIRECTED_SCENE = "directedScene";
  private static final String BUNDLE_CURRENT_MEDIA = "currentMedia";
  private static final String BUNDLE_NEW_MEDIA = "newMedia";
  private static final String BUNDLE_CHOSEN_REACTION = "chosenReaction";
  private static final String BUNDLE_STATE = "state";
  public final ObservableBoolean mPreviousMediaVisibility = new ObservableBoolean();
  public final ObservableBoolean mCancelButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSwitchCameraButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mSendButtonVisibility = new ObservableBoolean();
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean();
  public final ObservableBoolean mScenePreviewVisibility = new ObservableBoolean();
  public final ObservableBoolean mCameraPreviewVisibility = new ObservableBoolean();
  public final ObservableInt mCaptureButtonDrawableResource = new ObservableInt(CAPTURE_RESOURCE);

  private DirectingState mState = DirectingState.CAMERA;
  /**
   * The scene currently being directed.
   */
  private Scene mDirectedScene;
  /**
   * The media that is currently viewed and edited.
   */
  private Media mCurrentMedia;
  /**
   * A media that was just created and should be concatenated to {@link #mDirectedScene}.
   */
  private Media mNewMedia;
  /**
   * The reaction that should trigger the scene to follow up from {@link #mCurrentMedia} to {@link
   * #mNewMedia}.
   */
  private Emotion mChosenReaction;
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
          // TODO: add response data to Crashlytics
          Crashlytics.logException(new Exception("Scene saving failed."));
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
      Log.e(TAG, "Saving scene request to "
          + call.request().url()
          + " had failed. Scene: "
          + mDirectedScene, t);
      onPublishError();
    }
  };

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    if (savedInstanceState != null) {
      if (savedInstanceState.get(BUNDLE_DIRECTED_SCENE) != null) {
        mDirectedScene = savedInstanceState.getParcelable(BUNDLE_DIRECTED_SCENE);
      }
      if (savedInstanceState.get(BUNDLE_CHOSEN_REACTION) != null) {
        mChosenReaction = (Emotion) savedInstanceState.get(BUNDLE_CHOSEN_REACTION);
      }
      if (savedInstanceState.get(BUNDLE_CURRENT_MEDIA) != null) {
        mCurrentMedia = savedInstanceState.getParcelable(BUNDLE_CURRENT_MEDIA);
      }
      if (savedInstanceState.get(BUNDLE_NEW_MEDIA) != null) {
        mNewMedia = savedInstanceState.getParcelable(BUNDLE_NEW_MEDIA);
      }
      if (savedInstanceState.get(BUNDLE_STATE) != null) {
        mState = (DirectingState) savedInstanceState.getSerializable(BUNDLE_STATE);
      }
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mDirectedScene != null) {
      outState.putParcelable(BUNDLE_DIRECTED_SCENE, mDirectedScene);
    }
    if (mChosenReaction != null) {
      outState.putSerializable(BUNDLE_CHOSEN_REACTION, mChosenReaction);
    }
    if (mCurrentMedia != null) {
      outState.putParcelable(BUNDLE_CURRENT_MEDIA, mCurrentMedia);
    }
    if (mNewMedia != null) {
      outState.putParcelable(BUNDLE_NEW_MEDIA, mNewMedia);
    }
    if (mState != null) {
      outState.putSerializable(BUNDLE_STATE, mState);
    }
  }

  public Media getCurrentMedia() {
    return mCurrentMedia;
  }

  @Override public void onVisible() {
    super.onVisible();
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

  @Override public void onHidden() {
    super.onHidden();
    cancelSent();
  }

  @Override public void onPhotoTaken(Image image) {
    Log.d(TAG, "Photo taken.");
    mNewMedia = new Photo(CameraUtil.toByteArray(image));
    onEdit();
  }

  @Override public void onVideoRecorded(String videoPath) {
    Log.d(TAG, "Video recorded.");
    mCaptureButtonDrawableResource.set(CAPTURE_RESOURCE);
    mNewMedia = new Video(videoPath);
    onEdit();
  }

  @Override public void onVideoRecordStart() {
    mCaptureButtonDrawableResource.set(RECORD_RESOURCE);
  }

  /**
   * Goes back to edit the previous media, from which the user can reach the current one.
   */
  public void displayParentMedia() {
    // Should reach here only if the current media node has a parent.
    mCurrentMedia = mDirectedScene.getPreviousMedia(mCurrentMedia);
    onEdit();
  }

  public void onReactionChosen(Emotion reaction) {
    if (mDirectedScene.getNextMedia(mCurrentMedia, reaction) != null) {
      mCurrentMedia = mDirectedScene.getNextMedia(mCurrentMedia, reaction);
      onEdit();
    } else {
      mChosenReaction = reaction;
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
    mSwitchCameraButtonVisibility.set(false);
    mCancelButtonVisibility.set(false);
    mSendButtonVisibility.set(false);
    mLoadingImageVisibility.set(true);
    // Hide capture button
    if (getView() != null) {
      getView().hideToolbar();
    }
  }

  /**
   * Cancel the current media, so that you will not be judged for eternal embarrassment.
   */
  public void onCancel() {
    Log.d(TAG, "onCancel.");
    mCurrentMedia = mDirectedScene.removeMedia(mCurrentMedia);
    if (mCurrentMedia != null) {
      onEdit();
    } else {
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

  @VisibleForTesting public Emotion getChosenReaction() {
    return mChosenReaction;
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
        mCurrentMedia = mDirectedScene.getRootMedia();
      }
    } else {
      // Add media to directed scene
      if (mDirectedScene == null) {
        // No existing scene, so create one.
        mDirectedScene = new Scene(mNewMedia);
        mCurrentMedia = mNewMedia;
      } else {
        // Add media to flow tree
        if (mChosenReaction != null) {
          mDirectedScene.addMedia(mNewMedia, mCurrentMedia.getId(), mChosenReaction);
          mCurrentMedia = mNewMedia;
          mChosenReaction = null;
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
    mPreviousMediaVisibility.set(!mCurrentMedia.equals(mDirectedScene.getRootMedia()));
    // Hide capture button
    if (getView() != null) {
      getView().hideToolbar();
    }
    mSwitchCameraButtonVisibility.set(false);
    // Hides loading image.
    mLoadingImageVisibility.set(false);
    // Shows the directed scene preview, and hides the camera preview.
    mScenePreviewVisibility.set(true);
    mCameraPreviewVisibility.set(false);
    if (getView() != null) {
      getView().displayMedia(mCurrentMedia);
    }
  }

  private void onCamera() {
    Log.d(TAG, "Change state: " + DirectingState.CAMERA.name());
    mState = DirectingState.CAMERA;
    // Hides approval buttons.
    mCancelButtonVisibility.set(false);
    mSendButtonVisibility.set(false);
    // Exposes capture buttons.
    if (getView() != null) {
      getView().showToolbar();
    }
    mSwitchCameraButtonVisibility.set(true);
    // Hides loading image.
    mLoadingImageVisibility.set(false);
    // Hides the directed scene preview, and exposes the camera preview.
    mScenePreviewVisibility.set(false);
    mCameraPreviewVisibility.set(true);
    if (getView() != null) {
      getView().restoreCameraPreview();
      getView().removeMedia();
    }
    // Ensures state
    if (mDirectedScene != null && mChosenReaction == null) {
      mCurrentMedia = getDirectedScene().getRootMedia();
      onEdit();
    }
  }

  private void cancelSent() {
    if (mSaveSceneCall != null) {
      mSaveSceneCall.cancel();
    }
    if (mState == DirectingState.SENT && mLifecycleStage == LifecycleStage.STARTED) {
      onEdit();
    }
  }

  private void onPublished() {
    Log.d(TAG, "Change state: " + DirectingState.PUBLISHED.name());
    mState = DirectingState.PUBLISHED;
    if (getView() != null) {
      getView().toast(getContext().getString(R.string.saved_successfully));
      getView().navigateToTheater();
    }
  }

  private void onPublishError() {
    // TODO: use a dialog
    if (getView() != null) {
      getView().toast(getContext().getString(R.string.sent_failed));
    }
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
