package com.truethat.android.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import butterknife.BindString;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.common.network.InteractionApi;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.empathy.ReactionDetectionListener;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.EventType;
import com.truethat.android.model.InteractionEvent;
import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import java.util.Date;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 21/07/2017 for TrueThat.
 */

public class ReactableViewModel<Model extends Reactable>
    extends BaseFragmentViewModel<BaseFragmentViewInterface> implements ReactionDetectionListener {
  /**
   * Default for reaction counter's image view.
   */
  @VisibleForTesting public static final Emotion DEFAULT_REACTION_COUNTER = Emotion.HAPPY;
  public final ObservableInt mReactionDrawableResource =
      new ObservableInt(DEFAULT_REACTION_COUNTER.getDrawableResource());
  public final ObservableField<String> mReactionsCountText = new ObservableField<>("0");
  public final ObservableBoolean mDirectorNameVisibility = new ObservableBoolean(true);
  public final ObservableField<String> mTimeAgoText = new ObservableField<>();
  /**
   * Default for reaction counter's image view.
   */
  @VisibleForTesting @BindString(R.string.anonymous) String DEFAULT_DIRECTOR_NAME;
  public final ObservableField<String> mDirectorName = new ObservableField<>(DEFAULT_DIRECTOR_NAME);
  private Model mReactable;
  /**
   * API to inform our backend of user interaction with {@link #mReactable}, in the form of {@link
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
   * Whether the media resources to display this reactable had been downloaded.
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

  public Reactable getReactable() {
    return mReactable;
  }

  public void setReactable(Model reactable) {
    mReactable = reactable;
  }

  /**
   * Run once media resources of {@link #mReactable} had been downloaded, to the degree they can be
   * displayed to the user.
   */
  @CallSuper public void onReady() {
    Log.v(TAG, "onReady");
    mReadyForDisplay = true;
    if (getView().isReallyVisible()) {
      onDisplay();
    }
  }

  public boolean isReady() {
    return mReadyForDisplay;
  }

  @Override public String toString() {
    return TAG + " {" + mReactable + "}";
  }

  public void onReactionDetected(Emotion reaction) {
    if (mReactable.canReactTo(AppContainer.getAuthManager().getCurrentUser())) {
      Log.v(TAG, "Reaction detected: " + reaction.name());
      mReactable.doReaction(reaction);
      // Post event of reactable reaction.
      mPostEventCall = mInteractionApi.postEvent(
          new InteractionEvent(AppContainer.getAuthManager().getCurrentUser().getId(),
              mReactable.getId(), new Date(),
              EventType.REACTABLE_REACTION, mReactable.getUserReaction()));
      mPostEventCall.enqueue(mPostEventCallback);
      updateReactionCounters();
    }
    AppContainer.getReactionDetectionManager().unsubscribe(this);
  }

  /**
   * Run once the media resources of the {@link #mReactable} are ready and the view is visible.
   */
  @CallSuper void onDisplay() {
    Log.v(TAG, "onDisplay");
    doView();
    if (mReactable.canReactTo(AppContainer.getAuthManager().getCurrentUser())) {
      AppContainer.getReactionDetectionManager().subscribe(this);
    }
  }

  /**
   * Marks {@link #mReactable} as viewed by the user, and informs our backend about it.
   */
  private void doView() {
    // Don't record view of the user's own reactables.
    if (!mReactable.isViewed() && !AppContainer.getAuthManager()
        .getCurrentUser()
        .equals(mReactable.getDirector())) {
      mReactable.doView();
      mInteractionApi.postEvent(
          new InteractionEvent(AppContainer.getAuthManager().getCurrentUser().getId(),
              mReactable.getId(), new Date(),
              EventType.REACTABLE_VIEW, null)).enqueue(mPostEventCallback);
    }
  }

  /**
   * Updates the director layout with data from {@link #mReactable}.
   */
  private void updateInfoLayout() {
    if (mReactable.getDirector() != null) {
      // Sets director name.
      mDirectorName.set(mReactable.getDirector().getDisplayName());
      // Hide the director name if it is the user.
      if (mReactable.getDirector().equals(AppContainer.getAuthManager().getCurrentUser())) {
        mDirectorNameVisibility.set(false);
      }
    }
    // Sets time ago
    mTimeAgoText.set(DateUtil.formatTimeAgo(mReactable.getCreated()));
  }

  /**
   * Updates {@link R.id#reactionCounterLayout} with the counters of {@link
   * Reactable#getReactionCounters()} and an image based on {@link Reactable#getUserReaction()} or
   * {@link #DEFAULT_REACTION_COUNTER}.
   */
  private void updateReactionCounters() {
    long sumCounts = 0;
    for (Long counter : mReactable.getReactionCounters().values()) {
      sumCounts += counter;
    }
    if (sumCounts > 0) {
      // Abbreviates the counter.
      mReactionsCountText.set(NumberUtil.format(sumCounts));
      // Sets the proper emotion emoji.
      Emotion toDisplay = DEFAULT_REACTION_COUNTER;
      if (mReactable.getUserReaction() != null) {
        toDisplay = mReactable.getUserReaction();
      } else if (!mReactable.getReactionCounters().isEmpty()) {
        toDisplay = mReactable.getReactionCounters().lastKey();
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
  }
}
