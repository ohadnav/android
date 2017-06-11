package com.truethat.android.empathy;

import android.media.Image;
import android.support.annotation.Nullable;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 *
 * Interface for emotion detection classifiers.
 */

public interface EmotionDetectionClassifier {
  /**
   * @param image captured by the device's camera.
   * @return the most probable {@link Emotion} in {@code image}, or null if no confident classification could be made.
   */
  @Nullable Emotion classify(Image image);
}
