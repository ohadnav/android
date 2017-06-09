package com.truethat.android.empathy;

import android.media.Image;
import android.support.annotation.Nullable;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */

public interface EmotionDetectionAlgorithm {
  @Nullable Emotion detect(Image image);
}
