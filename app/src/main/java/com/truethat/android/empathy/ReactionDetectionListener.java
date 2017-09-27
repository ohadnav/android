package com.truethat.android.empathy;

import com.truethat.android.model.Emotion;
import com.truethat.android.viewmodel.viewinterface.BaseListener;

/**
 * Proudly created by ohad on 30/07/2017 for TrueThat.
 */

public interface ReactionDetectionListener extends BaseListener {
  void onReactionDetected(Emotion reaction);
}
