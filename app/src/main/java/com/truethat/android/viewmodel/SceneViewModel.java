package com.truethat.android.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
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
import com.truethat.android.model.Media;
import com.truethat.android.model.Scene;
import com.truethat.android.view.fragment.MediaFragment;
import com.truethat.android.viewmodel.viewinterface.SceneViewInterface;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 21/07/2017 for TrueThat.
 */

public class SceneViewModel extends BaseFragmentViewModel<SceneViewInterface>
    implements ReactionDetectionListener, MediaFragment.MediaListener {
  /**
   * Default color for reactions count.
   */
  @VisibleForTesting @ColorRes static final int DEFAULT_COUNT_COLOR = R.color.hint;
  /**
   * Emphasized color after a user reaction to a scene.
   */
  @VisibleForTesting @ColorRes static final int POST_REACTION_COUNT_COLOR = R.color.light;
  /**
   * Detection delay in milliseconds, so that reaction detection will be less affected by prior
   * events.
   */
  @VisibleForTesting static long DETECTION_DELAY_MILLIS = 300;
  public final ObservableInt mReactionDrawableResource =
      new ObservableInt(R.drawable.transparent_1x1);
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
  private Timer mDetectionTimer;
  /**
   * Whether the media had been displayed to the user. Used to limit the number of {@link
   * EventType#VIEW} events sent to one.
   */
  private Map<Media, Boolean> mMediaViewed = new HashMap<>();
  /**
   * A set of the detected reactions to each media. Used to limit the number of {@link
   * EventType#REACTION} events sent, to one per {@link Emotion}.
   */
  private Map<Media, Set<Emotion>> mDetectedReactions = new HashMap<>();
  /**
   * Currently displayed media.
   */
  private Media mCurrentMedia;
  /**
   * The next media after {@link #mCurrentMedia}, as per the user first reaction to {@link
   * #mCurrentMedia} and {@link Scene#mFlowTree}.
   */
  private Media mNextMedia;
  /**
   * The last detected reaction to {@link #mCurrentMedia}. Used to limit the number of animations
   * (i.e. {@link SceneViewInterface#bounceReactionImage()}) for subsequent identical reactions.
   */
  private Emotion mLastReaction;
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
  private Map<Media, Boolean> mMediaReady = new HashMap<>();

  public static void setDetectionDelayMillis(long detectionDelayMillis) {
    DETECTION_DELAY_MILLIS = detectionDelayMillis;
  }

  public Media getCurrentMedia() {
    return mCurrentMedia;
  }

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    // Initializes the API
    mInteractionApi = NetworkUtil.createApi(InteractionApi.class);
    mPostEventCallback = buildPostEventCallback();
  }

  @Override public void onBindView(@NonNull SceneViewInterface view) {
    super.onBindView(view);
    // This method is expected to be called once per view model and so string concatenation should
    // do.
    if (mScene != null) {
      TAG += " " + mScene.getId();
    }
  }

  @Override public void onStop() {
    super.onStop();
    if (mPostEventCall != null) mPostEventCall.cancel();
  }

  @Override public void onStart() {
    super.onStart();
    if (mCurrentMedia == null) {
      display(mScene.getRootMedia());
    } else {
      display(mCurrentMedia);
    }
    updateInfoLayout();
    updateReactionsLayout(null);
  }

  public void onVisible() {
    if (mDetectionTimer == null) mDetectionTimer = new Timer(TAG);
    if (mMediaReady.get(mCurrentMedia)) {
      onDisplay();
    }
  }

  @Override public void onHidden() {
    super.onHidden();
    AppContainer.getReactionDetectionManager().unsubscribe(this);
    if (mDetectionTimer != null) {
      mDetectionTimer.cancel();
      mDetectionTimer.purge();
      mDetectionTimer = null;
    }
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
  public void onReady() {
    Log.d(TAG, "onReady");
    mMediaReady.put(mCurrentMedia, true);
    if (getView().isReallyVisible()) {
      onDisplay();
    }
  }

  @Override public void onFinished() {
    Log.d(TAG, "Media finished");
    if (mNextMedia != null) {
      display(mNextMedia);
    }
  }

  public void onReactionDetected(Emotion reaction) {
    if (!mDetectedReactions.get(mCurrentMedia).contains(reaction)) {
      Log.v(TAG, "Reaction detected: " + reaction.name());
      mScene.doReaction(reaction);
      // Post event of scene reaction.
      InteractionEvent interactionEvent =
          new InteractionEvent(AppContainer.getAuthManager().getCurrentUser().getId(),
              mScene.getId(), new Date(), EventType.REACTION, reaction, mCurrentMedia.getId());
      mPostEventCall = mInteractionApi.postEvent(interactionEvent);
      mPostEventCall.enqueue(mPostEventCallback);
      if (!BuildConfig.DEBUG) {
        Crashlytics.setString(LoggingKey.LAST_INTERACTION_EVENT.name(),
            interactionEvent.toString());
      }
    }
    mDetectedReactions.get(mCurrentMedia).add(reaction);
    // Calculate next media
    if (mNextMedia == null) {
      mNextMedia = mScene.getNextMedia(mCurrentMedia, reaction);
      Log.d(TAG, "Next media: " + mNextMedia);
    }
    // Displays next media if it is finished.
    if (getView().hasMediaFinished() && mNextMedia != null) {
      display(mNextMedia);
    }
    // Show UI indication of detected reaction.
    if (mLastReaction != reaction) {
      updateReactionsLayout(reaction);
      getView().bounceReactionImage();
    }
    mReactionCountColor.set(POST_REACTION_COUNT_COLOR);
    // Update fragment state.
    mLastReaction = reaction;
  }

  @Override public boolean isReallyVisible() {
    return getView().isReallyVisible();
  }

  Media getNextMedia() {
    return mNextMedia;
  }

  Set<Emotion> getCurrentDetectedReactions() {
    return mDetectedReactions.get(mCurrentMedia);
  }

  /**
   * Run once the media resources of the {@link #mScene} are ready and the view is visible.
   */
  private void onDisplay() {
    Log.d(TAG, "onDisplay");
    doView();
    // Delays reaction detection by a bit.
    mDetectionTimer.schedule(new TimerTask() {
      @Override public void run() {
        if (!AppContainer.getAuthManager()
            .getCurrentUser()
            .getId()
            .equals(mScene.getDirector().getId())) {
          AppContainer.getReactionDetectionManager().subscribe(SceneViewModel.this);
        }
      }
    }, DETECTION_DELAY_MILLIS);
  }

  /**
   * Displays {@code media} to the user, and resets current state.
   *
   * @param media to display
   */
  private void display(Media media) {
    // Stops reaction detection temporarily until the new media is displayed.
    AppContainer.getReactionDetectionManager().unsubscribe(this);
    getView().display(media);
    // Updates media state.
    mCurrentMedia = media;
    mNextMedia = null;
    mLastReaction = null;
    if (!mMediaReady.containsKey(mCurrentMedia)) {
      mMediaReady.put(mCurrentMedia, false);
    }
    if (!mMediaViewed.containsKey(mCurrentMedia)) {
      mMediaViewed.put(mCurrentMedia, false);
    }
    if (!mDetectedReactions.containsKey(mCurrentMedia)) {
      mDetectedReactions.put(mCurrentMedia, new HashSet<Emotion>());
    }
  }

  /**
   * Marks {@link #mScene} as viewed by the user, and informs our backend about it.
   */
  private void doView() {
    if (!mMediaViewed.get(mCurrentMedia)) {
      InteractionEvent interactionEvent =
          new InteractionEvent(AppContainer.getAuthManager().getCurrentUser().getId(),
              mScene.getId(), new Date(), EventType.VIEW, null, mCurrentMedia.getId());
      mInteractionApi.postEvent(interactionEvent).enqueue(mPostEventCallback);
      if (!BuildConfig.DEBUG) {
        Crashlytics.setString(LoggingKey.LAST_INTERACTION_EVENT.name(),
            interactionEvent.toString());
      }
    }
    mMediaViewed.put(mCurrentMedia, true);
  }

  /**
   * Updates the director layout with data from {@link #mScene}.
   */
  private void updateInfoLayout() {
    if (mScene.getDirector() != null) {
      // Sets director name.
      mDirectorName.set(mScene.getDirector().getDisplayName());
      // Hide the director name if it is the user.
      if (mScene.getDirector()
          .getId()
          .equals(AppContainer.getAuthManager().getCurrentUser().getId())) {
        mDirectorNameVisibility.set(false);
      }
    }
    if (mScene.getCreated() != null) {
      // Sets time ago
      mTimeAgoText.set(DateUtil.formatTimeAgo(mScene.getCreated()));
    }
  }

  /**
   * Updates {@link R.id#reactionsCountLayout} with the counters of {@link
   * Scene#getReactionCounters()}.
   *
   * @param detectedReaction so that the displayed reaction matches it.
   */
  private void updateReactionsLayout(@Nullable Emotion detectedReaction) {
    long sumCounts = 0;
    for (Long counter : mScene.getReactionCounters().values()) {
      sumCounts += counter;
    }
    if (sumCounts > 0) {
      // Abbreviates the counter.
      mReactionsCountText.set(NumberUtil.format(sumCounts));
      // Sets the proper emotion emoji.
      if (detectedReaction != null) {
        mReactionDrawableResource.set(detectedReaction.getDrawableResource());
      } else if (!mScene.getReactionCounters().isEmpty()) {
        mReactionDrawableResource.set(mScene.getReactionCounters().lastKey().getDrawableResource());
      }
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
          Buffer buffer = new Buffer();
          try {
            //noinspection ConstantConditions
            call.request().body().writeTo(buffer);
          } catch (Exception e) {
            e.printStackTrace();
          }
          String requestBody = buffer.readUtf8();
          Log.e(TAG, "Failed to post event to "
              + call.request().url() + "\nBody: " + requestBody
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
