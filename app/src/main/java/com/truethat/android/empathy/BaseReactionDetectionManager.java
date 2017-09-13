package com.truethat.android.empathy;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.Log;
import com.truethat.android.model.Emotion;
import com.truethat.android.view.activity.BaseActivity;
import java.util.HashSet;
import java.util.Set;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public class BaseReactionDetectionManager implements ReactionDetectionManager {
  Set<ReactionDetectionListener> mReactionDetectionListeners = new HashSet<>();
  /**
   * For logging.
   */
  String TAG = this.getClass().getSimpleName();
  private State mState = State.IDLE;

  @Override public void start(@Nullable BaseActivity baseActivity) {
    Log.d(TAG, "Starting detection.");
    mState = State.DETECTING;
  }

  @Override public void subscribe(ReactionDetectionListener reactionDetectionListener) {
    if (isDetecting()) {
      Log.d(TAG, "Subscribing "
          + reactionDetectionListener.getClass().getSimpleName()
          + "("
          + reactionDetectionListener.hashCode()
          + ")");
      mReactionDetectionListeners.add(reactionDetectionListener);
    } else {
      Log.e(TAG, "Trying to subscribe to an idle manager.");
    }
  }

  @Override public void unsubscribe(ReactionDetectionListener reactionDetectionListener) {
    if (mReactionDetectionListeners.contains(reactionDetectionListener)) {
      Log.d(TAG, "Unsubscribing "
          + reactionDetectionListener.getClass().getSimpleName()
          + "("
          + reactionDetectionListener.hashCode()
          + ")");
      mReactionDetectionListeners.remove(reactionDetectionListener);
      Log.d(TAG, mReactionDetectionListeners.size() + " listeners left.");
    }
  }

  @Override public void stop() {
    if (mReactionDetectionListeners.isEmpty()) {
      Log.d(TAG, "Stopping detection.");
      mState = State.IDLE;
    } else {
      Log.d(TAG, "Not stopping: "
          + mReactionDetectionListeners.size()
          + " listeners left (such as "
          + mReactionDetectionListeners.iterator().next().getClass().getSimpleName()
          + ")");
    }
  }

  /**
   * @return Whether a detection is currently ongoing.
   */
  public boolean isDetecting() {
    return mState == State.DETECTING;
  }

  @CallSuper void onReactionDetected(Emotion reaction) {
    for (ReactionDetectionListener reactionDetectionListener : mReactionDetectionListeners) {
      reactionDetectionListener.onReactionDetected(reaction);
    }
  }

  private enum State {
    DETECTING, IDLE
  }
}
