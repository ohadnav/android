package com.truethat.android.empathy;

import android.media.Image;
import android.support.annotation.Nullable;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 *
 * Classifies all any {@code image} as null, i.e. as having no emotion in it.
 */

public class RandomEmotionDetectionClassifier implements EmotionDetectionClassifier {
  @Nullable @Override public Emotion classify(Image image) {
    return Emotion.values()[Math.min((int) Math.round(Math.random() * Emotion.values().length),
        Emotion.values().length - 1)];
  }
}
