package com.truethat.android.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import butterknife.BindString;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.LoggingKey;
import com.truethat.android.common.network.InteractionApi;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.empathy.ReactionDetectionListener;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.EventType;
import com.truethat.android.model.InteractionEvent;
import com.truethat.android.model.Scene;
import com.truethat.android.view.fragment.MediaFragment;
import com.truethat.android.viewmodel.viewinterface.SceneViewInterface;
import java.util.Date;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 21/07/2017 for TrueThat.
 */

public class SceneViewModel extends BaseFragmentViewModel<SceneViewInterface>
    implements ReactionDetectionListener, MediaFragment.MediaListener {
  /**
   * Default for reaction counter's image view.
   */
  @VisibleForTesting public static final Emotion DEFAULT_REACTION_COUNTER = Emotion.HAPPY;
  @VisibleForTesting @ColorRes static final int DEFAULT_COUNT_COLOR = R.color.hint;
  @VisibleForTesting @ColorRes static final int POST_REACTION_COUNT_COLOR = R.color.light;
  public final ObservableInt mReactionDrawableResource =
      new ObservableInt(DEFAULT_REACTION_COUNTER.getDrawableResource());
  public final ObservableInt mReactionCountColor = new ObservableInt(DEFAULT_COUNT_COLOR);
  public final ObservableField<String> mReactionsCountText = new ObservableField<>("0");
  public final ObservableBoolean mDirectorNameVisibility = new ObservableBoolean(true);
  public final ObservableBoolean mInfoLayoutVisibility = new ObservableBoolean(true);
  public final ObservableBoolean mReactionCountersVisibility = new ObservableBoolean(true);
  public final ObservableField<String> mTimeAgoText = new ObservableField<>();
  /**
   * Default for reaction counter's image view.
   */
  @VisibleForTesting @BindString(R.string.anonymous) String DEFAULT_DIRECTOR_NAME;
  public final ObservableField<String> mDirectorName = new ObservableField<>(DEFAULT_DIRECTOR_NAME);
  private Scene mScene;
  /**
   * API to inform our backend of user interaction with {@link #mScene}, in the form of {@link
   * InteractionEvent}.
   */
  private InteractionApi mInteractionApi;
  /**
   * HTTP POST call of {@link #mInteractionApi}.
   */
  private Call<ResponseBody> mPostEventCall;
  /**
   * Callback for {@link #mInteractionApi}.
   */
  private Callback<ResponseBody> mPostEventCallback;
  /**
   * Whether the media resources to display this scene had been downloaded.
   */
  private boolean mReadyForDisplay = false;

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    // Initializes the API
    mInteractionApi = NetworkUtil.createApi(InteractionApi.class);
    mPostEventCallback = buildPostEventCallback();
  }

  @CallSuper @Override public void onStop() {
    super.onStop();
    if (mPostEventCall != null) mPostEventCall.cancel();
  }

  @CallSuper @Override public void onStart() {
    super.onStart();
    updateInfoLayout();
    updateReactionCounters();
  }

  @CallSuper public void onVisible() {
    if (mReadyForDisplay) {
      onDisplay();
    }
  }

  @Override public void onHidden() {
    super.onHidden();
    AppContainer.getReactionDetectionManager().unsubscribe(this);
  }

  public Scene getScene() {
    return mScene;
  }

  public void setScene(Scene scene) {
    mScene = scene;
  }

  /**
   * Run once media resources of {@link #mScene} had been downloaded, to the degree they can be
   * displayed to the user.
   */
  @CallSuper public void onReady() {
    Log.d(TAG, "onReady");
    mReadyForDisplay = true;
    if (getView().isReallyVisible()) {
      onDisplay();
    }
  }

  @Override public String toString() {
    return TAG + " {" + mScene + "}";
  }

  public void onReactionDetected(Emotion reaction) {
    if (mScene.canReactTo(AppContainer.getAuthManager().getCurrentUser())) {
      Log.v(TAG, "Reaction detected: " + reaction.name());
      mScene.doReaction(reaction);
      // Post event of scene reaction.
      InteractionEvent interactionEvent =
          new InteractionEvent(AppContainer.getAuthManager().getCurrentUser().getId(),
              mScene.getId(), new Date(), EventType.REACTION, mScene.getUserReaction());
      mPostEventCall = mInteractionApi.postEvent(interactionEvent);
      mPostEventCall.enqueue(mPostEventCallback);
      if (!BuildConfig.DEBUG) {
        Crashlytics.setString(LoggingKey.LAST_INTERACTION_EVENT.name(),
            interactionEvent.toString());
      }
      updateReactionCounters();
      getView().bounceReactionImage();
    }
    AppContainer.getReactionDetectionManager().unsubscribe(this);
  }

  /**
   * Run once the media resources of the {@link #mScene} are ready and the view is visible.
   */
  @CallSuper void onDisplay() {
    Log.d(TAG, "onDisplay");
    doView();
    if (mScene.canReactTo(AppContainer.getAuthManager().getCurrentUser())) {
      AppContainer.getReactionDetectionManager().subscribe(this);
    }
  }

  /**
   * Marks {@link #mScene} as viewed by the user, and informs our backend about it.
   */
  private void doView() {
    // Don't record view of the user's own scenes.
    if (!mScene.isViewed() && !AppContainer.getAuthManager()
        .getCurrentUser().equals(mScene.getDirector())) {
      mScene.doView();
      InteractionEvent interactionEvent =
          new InteractionEvent(AppContainer.getAuthManager().getCurrentUser().getId(),
              mScene.getId(), new Date(), EventType.VIEW, null);
      mInteractionApi.postEvent(interactionEvent).enqueue(mPostEventCallback);
      if (!BuildConfig.DEBUG) {
        Crashlytics.setString(LoggingKey.LAST_INTERACTION_EVENT.name(),
            interactionEvent.toString());
      }
    }
  }

  /**
   * Updates the director layout with data from {@link #mScene}.
   */
  private void updateInfoLayout() {
    if (mScene.getDirector() != null) {
      // Sets director name.
      mDirectorName.set(mScene.getDirector().getDisplayName());
      // Hide the director name if it is the user.
      if (mScene.getDirector().equals(AppContainer.getAuthManager().getCurrentUser())) {
        mDirectorNameVisibility.set(false);
      }
    }
    // Sets time ago
    mTimeAgoText.set(DateUtil.formatTimeAgo(mScene.getCreated()));
  }

  /**
   * Updates {@link R.id#reactionCounterLayout} with the counters of {@link
   * Scene#getReactionCounters()} and an image based on {@link Scene#getUserReaction()} or
   * {@link #DEFAULT_REACTION_COUNTER}.
   */
  private void updateReactionCounters() {
    long sumCounts = 0;
    for (Long counter : mScene.getReactionCounters().values()) {
      sumCounts += counter;
    }
    if (sumCounts > 0) {
      // Abbreviates the counter.
      mReactionsCountText.set(NumberUtil.format(sumCounts));
      // Sets the proper emotion emoji.
      Emotion toDisplay = DEFAULT_REACTION_COUNTER;
      if (mScene.getUserReaction() != null) {
        mReactionCountColor.set(POST_REACTION_COUNT_COLOR);
        toDisplay = mScene.getUserReaction();
      } else if (!mScene.getReactionCounters().isEmpty()) {
        toDisplay = mScene.getReactionCounters().lastKey();
      }
      mReactionDrawableResource.set(toDisplay.getDrawableResource());
    } else {
      mReactionsCountText.set("");
      mReactionDrawableResource.set(R.drawable.transparent_1x1);
    }
  }

  private Callback<ResponseBody> buildPostEventCallback() {
    return new Callback<ResponseBody>() {
      @Override public void onResponse(@NonNull Call<ResponseBody> call,
          @NonNull Response<ResponseBody> response) {
        if (!response.isSuccessful()) {
          if (!BuildConfig.DEBUG) {
            Crashlytics.logException(new Exception("Failed to save interaction event"));
          }
          Log.e(TAG, "Failed to post event to "
              + call.request().url()
              + "\nRequest body: "
              + call.request().body()
              + "\nResponse: "
              + response.code()
              + " "
              + response.message()
              + "\n"
              + response.headers());
        }
      }

      @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
        if (!BuildConfig.DEBUG) {
          Crashlytics.logException(t);
        }
        t.printStackTrace();
        Log.e(TAG, "Post event request to " + call.request().url() + " had failed.", t);
      }
    };
  }
}
