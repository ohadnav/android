package com.truethat.android.empathy;

import android.media.Image;
import com.truethat.android.ui.common.camera.CameraFragment;

/**
 * Proudly created by ohad on 08/06/2017 for TrueThat.
 *
 * Communication interface through which {@link ReactionDetectionModule} publishes its
 * classification and subscribes to requests input.
 */

public interface ReactionDetectionPubSub {
  /**
   * Handler for emotion reaction detected event.
   *
   * @param reaction as determined by {@link ReactionDetectionModule}.
   */
  void onReactionDetected(Emotion reaction);

  /**
   * Request additional input (i.e. takes a photo with the device's camera, or calls {@link
   * CameraFragment#takePicture()}), in order to invoke {@link ReactionDetectionModule#attempt(Image)}
   */
  void requestInput();
}
