package com.truethat.android.empathy;

import android.media.Image;
import com.truethat.android.auth.User;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 */
public interface ReactionDetectionModule {
  /**
   * Initiates an emotional reaction detection task. The keyword here is <b>reaction</b>. As the reaction timing
   * varies, we have to capture images continuously until {@link EmotionDetectionClassifier} can determine the most
   * probable {@link Emotion} reaction.
   *
   * @param detectionPubSub communication interface through which the detection module publishes its classification and
   * subscribes to requests input.
   */
  void detect(ReactionDetectionPubSub detectionPubSub);

  /**
   * Attempts to classify the current {@link User}'s {@link Emotion} in {@code image}.
   *
   * @param image input for {@link EmotionDetectionClassifier}.
   */
  void attempt(Image image);

  /**
   * Stops the current detection task.
   */
  void stop();
}
