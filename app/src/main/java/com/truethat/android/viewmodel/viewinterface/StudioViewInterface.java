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
  void onPublished();

  /**
   * Triggered as per {@link StudioViewModel.DirectingState#APPROVAL}, usually after a {@link Reactable} is created and (i.e. an image was taken but not sent yet).
   */
  void onApproval();

  /**
   * Triggered as per {@link StudioViewModel.DirectingState#SENT}, usually after a {@link Reactable} is approved and being sent to the backend for saving.
   */
  void onSent();

  /**
   * Triggered as per {@link StudioViewModel.DirectingState#DIRECTING}, when a reactable is being created (i.e. whilst taking a picture).
   */
  void onDirecting();

  /**
   * Displays a preview of the directed reactable, so that the user can approve it or prevent
   * eternal shame.
   *
   * @param reactable to preview.
   */
  void displayPreview(Reactable reactable);
}
