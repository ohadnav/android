package com.truethat.android.empathy;

import com.affectiva.android.affdex.sdk.detector.Detector;
import com.truethat.android.model.Emotion;
import com.truethat.android.viewmodel.viewinterface.BaseListener;

/**
 * Proudly created by ohad on 30/07/2017 for TrueThat.
 */

public interface ReactionDetectionListener extends BaseListener, Detector.FaceListener {
  /**
   * @param reaction   detected on our user's pretty face.
   * @param mostLikely the emotion most likely to represent the user's mood. Used for cases when we
   *                   want to detect a specific emotion, regardless of whether it is the prevalent
   *                   one.
   */
  void onReactionDetected(Emotion reaction, boolean mostLikely);
}
