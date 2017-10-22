package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.view.fragment.RepertoireFragment;
import com.truethat.android.view.fragment.StudioFragment;
import com.truethat.android.view.fragment.TheaterFragment;

/**
 * Proudly created by ohad on 10/10/2017 for TrueThat.
 */

public interface ToolbarViewInterface {
  /**
   * Navigates to {@link TheaterFragment}.
   */
  void navigateToTheater();

  /**
   * Navigates to {@link StudioFragment}.
   */
  void navigateToStudio();

  /**
   * Navigates to {@link RepertoireFragment}.
   */
  void navigateToRepertoire();

  /**
   * Hides the entire toolbar
   */
  void hideToolbar();

  /**
   * Exposes the toolbar (and the capture button).
   */
  void showToolbar();
}
