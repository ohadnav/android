package com.truethat.android.empathy;

import android.util.Log;
import com.truethat.android.model.Emotion;

/**
 * Proudly created by ohad on 18/06/2017 for TrueThat.
 */

public class FakeReactionDetectionManager extends BaseReactionDetectionManager {

  /**
   * Mocks a reaction detection.
   */
  @Override public void onReactionDetected(Emotion reaction, boolean mostLikely) {
    Log.d(TAG, "FAKE onReactionDetected(" + reaction.name() + ", " + mostLikely + ")");
    super.onReactionDetected(reaction, mostLikely);
  }

  @Override public void onFaceDetectionStarted() {
    Log.d(TAG, "FAKE onFaceDetectionStarted");
    super.onFaceDetectionStarted();
  }

  @Override public void onFaceDetectionStopped() {
    Log.d(TAG, "FAKE onFaceDetectionStopped");
    super.onFaceDetectionStopped();
  }

  /**
   * @param listener to check for
   *
   * @return whether to given listener is subscribed to this detection manager.
   */
  public boolean isSubscribed(ReactionDetectionListener listener) {
    return isDetecting() && mReactionDetectionListeners.contains(listener);
  }
}
