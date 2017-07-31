package com.truethat.android.empathy;

import android.media.Image;
import android.util.Log;
import com.truethat.android.model.Emotion;

/**
 * Proudly created by ohad on 18/06/2017 for TrueThat.
 */

public class FakeReactionDetectionManager extends BaseReactionDetectionManager {
  /**
   * Mocks a reaction detection.
   *
   * @param reaction of the detected emotion.
   */
  public void doDetection(Emotion reaction) {
    Log.v(TAG, "Faking detection of " + reaction.name() + ".");
    onReactionDetected(reaction);
  }

  /**
   * @param listener to check for
   * @return whether to given listener is subscribed to this detection manager.
   */
  public boolean isSubscribed(ReactionDetectionListener listener) {
    return isDetecting() && mReactionDetectionListeners.contains(listener);
  }
}
