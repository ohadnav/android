package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.model.Media;

/**
 * Proudly created by ohad on 30/08/2017 for TrueThat.
 */

public interface SceneViewInterface extends BaseFragmentViewInterface {
  /**
   * Bounces reaction image, to indicate the user her reaction was captured.
   */
  void bounceReactionImage();

  /**
   * Displays the media.
   *
   * @param media to create a fragment from.
   */
  void display(Media media);

  /**
   * @return whether the currently displayed media has finished.
   */
  boolean hasMediaFinished();
}
