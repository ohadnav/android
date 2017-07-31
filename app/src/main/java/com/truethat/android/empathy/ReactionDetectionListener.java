package com.truethat.android.empathy;

import android.util.Log;
import com.affectiva.android.affdex.sdk.Frame;
import com.affectiva.android.affdex.sdk.detector.Face;
import com.affectiva.android.affdex.sdk.detector.FrameDetector;
import com.truethat.android.model.Emotion;
import java.util.List;

/**
 * Proudly created by ohad on 30/07/2017 for TrueThat.
 */

public interface ReactionDetectionListener {
  void onReactionDetected(Emotion reaction);
}
