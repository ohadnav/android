package com.truethat.android.ui.common.media;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.network.InteractionAPI;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.empathy.ReactionDetectionModule;
import com.truethat.android.empathy.ReactionDetectionPubSub;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.EventType;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.ReactableEvent;
import com.truethat.android.model.Scene;
import com.truethat.android.ui.common.BaseFragment;
import java.util.Date;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A generic container for {@link Reactable}. Handles touch gestures for navigation between {@link
 * ReactableFragment}, and emotional reaction detection.
 */
public abstract class ReactableFragment<T extends Reactable> extends BaseFragment {
  /**
   * Default for reaction counter's image view.
   */
  @VisibleForTesting public static final Emotion DEFAULT_REACTION_COUNTER = Emotion.HAPPY;
  private static final String ARG_REACTABLE = "reactable";

  protected T mReactable;
  @BindView(R.id.directorNameText) TextView mDirectorNameText;
  @BindView(R.id.timeAgoText) TextView mTimeAgoText;
  /**
   * Communication interface with parent activity.
   */
  private OnReactableInteractionListener mListener;
  /**
   * API to inform our backend of user interaction with {@link #mReactable}, in the form of {@link
   * ReactableEvent}.
   */
  private InteractionAPI mInteractionAPI;
  /**
   * HTTP POST call of {@link #mInteractionAPI}.
   */
  private Call<ResponseBody> mPostEventCall;
  /**
   * Callback for {@link #mInteractionAPI}.
   */
  private Callback<ResponseBody> mPostEventCallback;
  /**
   * Communication interface with {@link ReactionDetectionModule}.
   */
  private ReactionDetectionPubSub mDetectionPubSub;
  /**
   * Whether the media resources to display this reactable had been downloaded.
   */
  private boolean mReadyForDisplay = false;

  public ReactableFragment() {
    // Required empty public constructor
  }

  /**
   * Prepares {@code fragment} for creation in implementing class, such as {@link
   * SceneFragment#newInstance(Scene)}.
   *
   * @param fragment  to prepare.
   * @param reactable to associate with this fragment.
   */
  public static void prepareInstance(Fragment fragment, Reactable reactable) {
    Bundle args = new Bundle();
    args.putSerializable(ARG_REACTABLE, reactable);
    fragment.setArguments(args);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnReactableInteractionListener) {
      mListener = (OnReactableInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement "
          + OnReactableInteractionListener.class.getSimpleName());
    }
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //noinspection unchecked
    mReactable = (T) getArguments().getSerializable(ARG_REACTABLE);
    // Initializes the Theater API
    mInteractionAPI = NetworkUtil.createAPI(InteractionAPI.class);
    mPostEventCallback = buildPostEventCallback();
    mDetectionPubSub = buildDetectionPubSub();
  }

  /**
   * Creation of media layout, such as a Scene image, is done by implementations i.e. in {@link
   * #createMedia(LayoutInflater, Bundle)} )}.
   */
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    updateDirectorLayout();
    updateReactionCounters();
    createMedia(inflater, savedInstanceState);
    return mRootView;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    // Media layout will be created again in #onCreateView, and so the reactable is not ready.
    mReadyForDisplay = false;
  }

  @Override public void onDetach() {
    super.onDetach();
    mListener = null;
    if (mPostEventCall != null) mPostEventCall.cancel();
  }

  @Override public void onVisible() {
    super.onVisible();
    if (mReadyForDisplay) {
      onDisplay();
    }
  }

  @Override public void onHidden() {
    super.onHidden();
    App.getReactionDetectionModule().stop();
  }

  @Override protected int getLayoutResId() {
    return R.layout.fragment_reactable;
  }

  public T getReactable() {
    return mReactable;
  }

  /**
   * Run once media resources of {@link #mReactable} had been downloaded, to the degree they can be
   * displayed to the user.
   */
  public void onReady() {
    Log.v(TAG, "onReady");
    mReadyForDisplay = true;
    if (isReallyVisible()) {
      onDisplay();
    }
  }

  /**
   * Run once the media resources of the {@link #mReactable} are ready and this fragment satisfies
   * {@link #isReallyVisible()}.
   */
  public void onDisplay() {
    Log.v(TAG, "onDisplay");
    doView();
    if (mReactable.canReactTo()) {
      App.getReactionDetectionModule().detect(mDetectionPubSub);
    }
  }

  public boolean isReady() {
    return mReadyForDisplay;
  }

  @Override public String toString() {
    return TAG + " {" + mReactable + "}";
  }

  /**
   * Create the media layout of the fragment, such as the {@link Scene} image.
   */
  @MainThread protected abstract void createMedia(LayoutInflater inflater,
      Bundle savedInstanceState);

  /**
   * Marks {@link #mReactable} as viewed by the user, and informs our backend about it.
   */
  protected void doView() {
    // Don't record view of the user's own reactables.
    if (!mReactable.isViewed() && !mReactable.getDirector().equals(App.getAuthModule().getUser())) {
      mReactable.doView();
      mInteractionAPI.postEvent(
          new ReactableEvent(App.getAuthModule().getUser().getId(), mReactable.getId(), new Date(),
              EventType.REACTABLE_VIEW, null)).enqueue(mPostEventCallback);
    }
  }

  @MainThread private void doReaction() {
    updateReactionCounters();
  }

  /**
   * Updates the director layout with data from {@link #mReactable}.
   */
  @MainThread private void updateDirectorLayout() {
    // Sets director name.
    mDirectorNameText.setText(mReactable.getDirector().getDisplayName());
    // Hide the director name if it is the user.
    if (mReactable.getDirector().equals(App.getAuthModule().getUser())) {
      mDirectorNameText.setVisibility(View.GONE);
    }
    // Sets time ago
    mTimeAgoText.setText(DateUtil.formatTimeAgo(mReactable.getCreated()));
  }

  /**
   * Updates {@link R.id#reactionCounterLayout} with the counters of {@link
   * Reactable#getReactionCounters()} and an image based on {@link Reactable#getUserReaction()} or
   * {@link #DEFAULT_REACTION_COUNTER}.
   */
  @MainThread private void updateReactionCounters() {
    long sumCounts = 0;
    for (Long counter : mReactable.getReactionCounters().values()) {
      sumCounts += counter;
    }
    // Abbreviates the counter.
    TextView reactionCountText = (TextView) mRootView.findViewById(R.id.reactionCountText);
    reactionCountText.setText(NumberUtil.format(sumCounts));
    // Sets the proper emotion emoji.
    Emotion toDisplay = DEFAULT_REACTION_COUNTER;
    if (mReactable.getUserReaction() != null) {
      toDisplay = mReactable.getUserReaction();
    } else if (!mReactable.getReactionCounters().isEmpty()) {
      toDisplay = mReactable.getReactionCounters().lastKey();
    }
    ImageView imageView = (ImageView) mRootView.findViewById(R.id.reactionImage);
    imageView.setImageResource(toDisplay.getDrawableResource());
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

  private ReactionDetectionPubSub buildDetectionPubSub() {
    return new ReactionDetectionPubSub() {
      /**
       * Timestamp of the reaction itself. Since we cannot exactly determine when the reaction
       * occurred, we use the
       * timestamp of image of the reaction.
       */
      private Date mRealEventTime;

      @Override public void onReactionDetected(Emotion reaction) {
        if (mReactable.getUserReaction() == null) {
          Log.v(TAG, "Reaction detected: " + reaction.name());
          mReactable.doReaction(reaction);
          // Post event of reactable reaction.
          mPostEventCall = mInteractionAPI.postEvent(
              new ReactableEvent(App.getAuthModule().getUser().getId(), mReactable.getId(),
                  mRealEventTime, EventType.REACTABLE_REACTION, mReactable.getUserReaction()));
          mPostEventCall.enqueue(mPostEventCallback);
          // Triggers the reaction visual outcome.
          getActivity().runOnUiThread(new Runnable() {
            @Override public void run() {
              ReactableFragment.this.doReaction();
            }
          });
        } else {
          Log.v(TAG, "Second time reaction " + reaction.name() + " is ignored.");
        }
      }

      @Override public void requestInput() {
        mListener.requestDetectionInput();
        mRealEventTime = new Date();
      }
    };
  }

  public interface OnReactableInteractionListener {
    /**
     * Request an image input for reaction detection.
     */
    void requestDetectionInput();
  }
}
