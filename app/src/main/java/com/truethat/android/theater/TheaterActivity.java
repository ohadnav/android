package com.truethat.android.theater;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.Scene;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.network.EventType;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.empathy.Reactable;
import com.truethat.android.empathy.ReactionDetectionPubSub;
import com.truethat.android.studio.StudioActivity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends CameraActivity {
  /**
   * The displayed reactable index in {@code mReactablesAndLayouts}.
   */
  private int mDisplayedReactableIndex = -1;
  /**
   * A list of reactables and their respective UI layouts. Layouts are inflated as soon as the reactables are get from
   * the server to create a smoother experience.
   */
  private List<Pair<Reactable, SceneLayout>> mReactablesAndLayouts = new ArrayList<>();
  private ViewGroup mRootView;
  /**
   * Reactables are loading indicator
   */
  private ProgressBar mProgressBar;
  private TheaterAPI mTheaterAPI;
  private Callback<ResponseBody> mPostEventCallback = new Callback<ResponseBody>() {
    @Override public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
      if (!response.isSuccessful()) {
        Log.e(TAG, "Failed to post event to "
            + call.request().url()
            + "\n"
            + response.code()
            + " "
            + response.message()
            + "\n"
            + response.headers());
      }
    }

    @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
      Log.e(TAG, "Post event request to " + call.request().url() + " had failed.", t);
    }
  };
  private Callback<List<Scene>> mGetScenesCallback = new Callback<List<Scene>>() {
    @Override public void onResponse(@NonNull Call<List<Scene>> call, @NonNull Response<List<Scene>> response) {
      if (response.isSuccessful()) {
        int toDisplayIndex = mReactablesAndLayouts.size();
        List<Scene> newScenes = response.body();
        assert newScenes != null;
        for (Scene newScene : newScenes) {
          mReactablesAndLayouts.add(new Pair<Reactable, SceneLayout>(newScene, new SceneLayout(newScene, mRootView)));
        }
        if (!newScenes.isEmpty()) {
          displayScene(toDisplayIndex);
        }
      } else {
        Log.e(TAG, "Failed to get scenes from "
            + call.request().url()
            + "\n"
            + response.code()
            + " "
            + response.message()
            + "\n"
            + response.headers());
      }
    }

    @Override public void onFailure(@NonNull Call<List<Scene>> call, @NonNull Throwable t) {
      Log.e(TAG, "Fetch scenes request to " + call.request().url() + " had failed.", t);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_theater);
    // Animation for screen transitions.
    this.overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left);
    // Initializes UI elements.
    mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    // Hooks for screen swipes
    mRootView = (ViewGroup) findViewById(R.id.theaterActivity);
    mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeRight() {
        startActivity(new Intent(TheaterActivity.this, StudioActivity.class));
      }

      @Override public void onSwipeDown() {
        nextScene();
      }

      @Override public void onSwipeUp() {
        previousScene();
      }
    });
    // Initializes the Theater API
    mTheaterAPI = NetworkUtil.createAPI(TheaterAPI.class);
  }

  @Override protected void onStart() {
    super.onStart();
    // TODO(ohad): load activity with previously displayed scenes.
    getScenes();
  }

  @Override protected void onPause() {
    super.onPause();
    App.getReactionDetectionModule().stop();
  }

  @Override protected void onResume() {
    super.onResume();
    startEmotionalReactionDetection();
  }

  @Override protected void processImage() {
    // Pushes new input to the detection module.
    App.getReactionDetectionModule().attempt(supplyImage());
  }

  /**
   * Navigates to previous scene in {@code mReactablesAndLayouts}.
   */
  // TODO(ohad): Instagram like horizontal progress for scenes transition.
  private void previousScene() {
    Log.v(TAG, "Previous scene");
    if (mDisplayedReactableIndex <= 0) return;
    displayScene(mDisplayedReactableIndex - 1);
  }

  /**
   * Navigates to next scene in {@code mReactablesAndLayouts}.
   */
  // TODO(ohad): automatically progress to next scene.
  private void nextScene() {
    Log.v(TAG, "Next scene");
    if (mDisplayedReactableIndex >= mReactablesAndLayouts.size() - 1) {
      // If all scenes had already been viewed, then get new ones.
      getScenes();
    } else {
      displayScene(mDisplayedReactableIndex + 1);
    }
  }

  /**
   * Adds the UI layout of the matching scene to the root view, starts a detection task and posts a view event.
   * @param displayIndex in {@code mReactablesAndLayouts}.
   */
  private void displayScene(int displayIndex) {
    if (displayIndex < 0 || displayIndex >= mReactablesAndLayouts.size()) {
      IndexOutOfBoundsException e = new IndexOutOfBoundsException();
      Log.e(TAG, displayIndex + " is not a scene index.", e);
      throw e;
    }
    Log.v(TAG, "Displaying scene " + displayIndex);
    mDisplayedReactableIndex = displayIndex;
    runOnUiThread(new Runnable() {
      @Override public void run() {
        // Hides default image.
        ImageView imageView = (ImageView) TheaterActivity.this.findViewById(R.id.defaultImage);
        imageView.setVisibility(GONE);
        // Hides loading animation.
        mProgressBar.setVisibility(GONE);
        // Removes old scene, if it exists.
        View currentLayout = TheaterActivity.this.findViewById(R.id.sceneLayout);
        if (currentLayout != null) {
          mRootView.removeView(currentLayout);
        }
        // TODO(ohad): add transition animation.
        // Adds the new scene.
        mRootView.addView(mReactablesAndLayouts.get(mDisplayedReactableIndex).second.getLayout());
        startEmotionalReactionDetection();
      }
    });
    // Post event of scene view.
    mTheaterAPI.postEvent(new ReactableEvent(App.getAuthModule().getUser(this).getId(),
        mReactablesAndLayouts.get(mDisplayedReactableIndex).first.getId(), new Date(),
        EventType.REACTABLE_VIEW, null))
        .enqueue(mPostEventCallback);
  }

  @VisibleForTesting void startEmotionalReactionDetection() {
    Log.v(TAG, "Starting emotional reaction detection.");
    // Check if a reactable is displayed.
    if (mDisplayedReactableIndex >= 0) {
      Reactable displayedReactable = mReactablesAndLayouts.get(mDisplayedReactableIndex).first;
      // Allow only the first reaction. Because like so many things... the first time feels most real ;)
      if (displayedReactable.getUserReaction() == null) {
        // Starts emotional reaction detection. Any previous detection is immediately stopped.
        App.getReactionDetectionModule().detect(buildReactionDetectionPubSub(displayedReactable));
      }
    }
  }

  /**
   * Applies the reactable reaction (i.e. {@link Reactable#getUserReaction()}) to the UI.
   * @param reactable to which the user had reacted.
   */
  private void doReaction(final Reactable reactable) {
    // Verify that the user had indeed reacted to the reactable.
    if (reactable.getUserReaction() == null) {
      Log.e(TAG, "Emotionless user! And careless programmer.");
      throw new IllegalArgumentException(
          "User had not yet reacted to this dramatic reactable (ID = " + reactable.getId() + ")");
    }
    // Checks reactable is currently displayed.
    if (mReactablesAndLayouts.get(mDisplayedReactableIndex).first.equals(reactable)) {
      // Applies emotional reaction onto the reactable layout.
      runOnUiThread(new Runnable() {
        @Override public void run() {
          mReactablesAndLayouts.get(mDisplayedReactableIndex).second.doReaction(reactable.getUserReaction());
        }
      });
    }
  }

  /**
   * Fetching more scenes from our magnificent backend.
   */
  private void getScenes() {
    Log.v(TAG, "Fetching scenes...");
    runOnUiThread(new Runnable() {
      @Override public void run() {
        mProgressBar.setVisibility(View.VISIBLE);
      }
    });
    mTheaterAPI.getScenes().enqueue(mGetScenesCallback);
  }

  // A method is used since a new instance of an inner class cannot be created in tests.
  @VisibleForTesting TheaterReactionDetectionPubSub buildReactionDetectionPubSub(Reactable reactable) {
    return new TheaterReactionDetectionPubSub(reactable);
  }

  private class TheaterReactionDetectionPubSub implements ReactionDetectionPubSub {
    // For which is the reaction should be detected.
    private Reactable mReactable;
    /**
     * Timestamp of the reaction itself. Since we cannot exactly determine when the reaction occurred, we use the
     * timestamp of image of the reaction.
     */
    private Date mRealEventTime;

    private TheaterReactionDetectionPubSub(Reactable reactable) {
      mReactable = reactable;
    }

    @Override public void onReactionDetected(Emotion reaction) {
      // Ensuring this is the first reaction.
      if (mReactable.getUserReaction() == null) {
        Log.v(TAG, "Reaction detected: " + reaction.name());
        mReactable.doReaction(reaction);
        // Post event of reactable reaction.
        mTheaterAPI.postEvent(
            new ReactableEvent(App.getAuthModule().getUser(TheaterActivity.this).getId(),
                mReactable.getId(),
                mRealEventTime, EventType.REACTABLE_REACTION, mReactable.getUserReaction()))
            .enqueue(mPostEventCallback);
        // Triggers the reaction visual outcome.
        TheaterActivity.this.doReaction(mReactable);
      } else {
        Log.v(TAG, "Second time reaction " + reaction.name() + " is ignored.");
      }
    }

    @Override public void requestInput() {
      mRealEventTime = new Date();
      takePicture();
    }
  }
}


