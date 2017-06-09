package com.truethat.android.empathy;

import android.media.Image;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */
public interface ReactionDetectionModule {
  void detect(ReactionDetectionPubSub detectionPubSub);

  void pushInput(Image image);

  void stop();
}
