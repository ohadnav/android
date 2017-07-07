package com.truethat.android.empathy;

import android.media.Image;
import android.support.annotation.Nullable;
import com.truethat.android.model.Emotion;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 * <p>
 * Classifies all any {@code image} as null, i.e. as having no emotion in it.
 */

public class NullEmotionDetectionClassifier implements EmotionDetectionClassifier {
  @Nullable @Override public Emotion classify(Image image) {
    return null;
  }
}
