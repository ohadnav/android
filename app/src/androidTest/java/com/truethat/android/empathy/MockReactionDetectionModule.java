package com.truethat.android.empathy;

import android.media.Image;

/**
 * Proudly created by ohad on 18/06/2017 for TrueThat.
 */

public class MockReactionDetectionModule implements ReactionDetectionModule {
  private ReactionDetectionPubSub mDetectionPubSub;

  @Override public void detect(ReactionDetectionPubSub detectionPubSub) {
    mDetectionPubSub = detectionPubSub;
  }

  @Override public void attempt(Image image) {

  }

  @Override public void stop() {
    mDetectionPubSub = null;
  }

  /**
   * Mocks a reaction detection.
   *
   * @param reaction of the detected emotion.
   */
  public void doDetection(Emotion reaction) {
    if (mDetectionPubSub != null) {
      mDetectionPubSub.onReactionDetected(reaction);
      mDetectionPubSub = null;
    } else {
      throw new IllegalStateException("Detection was stopped or was not started.");
    }
  }

  /**
   * @return Whether a detection is currently ongoing.
   */
  public boolean isDetecting() {
    return mDetectionPubSub != null;
  }

  /**
   * Requests an input to initiate an iteration of reaction detection.
   */
  public void next() {
    if (mDetectionPubSub != null) {
      mDetectionPubSub.requestInput();
    } else {
      throw new IllegalStateException("Detection was stopped or was not started.");
    }
  }
}
