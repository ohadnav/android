package com.truethat.android.empathy;

import android.support.annotation.CallSuper;
import android.util.Log;
import com.truethat.android.model.Emotion;
import java.util.HashSet;
import java.util.Set;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public class BaseReactionDetectionManager implements ReactionDetectionManager {
   Set<ReactionDetectionListener> mReactionDetectionListeners;
  /**
   * For logging.
   */
  String TAG = this.getClass().getSimpleName();
  private State mState = State.IDLE;

  @Override public void start() {
    Log.v(TAG, "Starting detection.");
    mReactionDetectionListeners = new HashSet<>();
    mState = State.DETECTING;
  }

  @Override public void subscribe(ReactionDetectionListener reactionDetectionListener) {
    if (isDetecting()) {
      Log.v(TAG, "Subscribing "
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
    if (isDetecting()) {
      Log.v(TAG, "Unsubscribing "
          + reactionDetectionListener.getClass().getSimpleName()
          + "("
          + reactionDetectionListener.hashCode()
          + ")");
      mReactionDetectionListeners.remove(reactionDetectionListener);
      Log.v(TAG, mReactionDetectionListeners.size() + " listeners left.");
    }
  }

  @Override public void stop() {
    Log.v(TAG, "Stopping detection.");
    mReactionDetectionListeners = new HashSet<>();
    mState = State.IDLE;
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
    DETECTING,
    IDLE
  }
}
