package com.truethat.android.empathy;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */

public interface ReactionDetectionPubSub {
  void onReactionDetected(Emotion emotion);

  void requestInput();
}
