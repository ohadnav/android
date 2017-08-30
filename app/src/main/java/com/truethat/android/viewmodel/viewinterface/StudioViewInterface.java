package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.StudioViewModel;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

public interface StudioViewInterface extends BaseViewInterface {
  /**
   * Triggered as per {@link StudioViewModel.DirectingState#PUBLISHED}. Usually after a {@link
   * Reactable} is saved on our backend.
   */
  void leaveStudio();

  /**
   * Restores camera preview.
   */
  void restoreCameraPreview();

  /**
   * Displays a preview of the directed reactable, so that the user can approve it or prevent
   * eternal shame.
   *
   * @param reactable to preview.
   */
  void displayPreview(Reactable reactable);
}
