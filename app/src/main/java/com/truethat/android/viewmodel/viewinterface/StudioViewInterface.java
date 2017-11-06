package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.model.Media;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

//public interface StudioViewInterface extends BaseFragmentViewInterface, ToolbarViewInterface {
public interface StudioViewInterface extends BaseViewInterface, ToolbarViewInterface {
  /**
   * Restores camera preview.
   */
  void restoreCameraPreview();

  /**
   * Displays a preview of the directed scene, so that the user can approve it or prevent
   * eternal shame.
   *
   * @param media to preview.
   */
  void displayMedia(Media media);

  /**
   * Removes media display, to prevent interference with camera use.
   */
  void removeMedia();
}
