package com.truethat.android.empathy;

import com.truethat.android.model.Emotion;

/**
 * Proudly created by ohad on 30/07/2017 for TrueThat.
 */

public interface ReactionDetectionListener {
  void onReactionDetected(Emotion reaction);
}
