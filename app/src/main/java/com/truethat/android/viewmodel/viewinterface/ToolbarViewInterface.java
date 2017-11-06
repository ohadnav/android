package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.view.activity.RepertoireActivity;
import com.truethat.android.view.activity.StudioActivity;
import com.truethat.android.view.activity.TheaterActivity;

/**
 * Proudly created by ohad on 10/10/2017 for TrueThat.
 */

public interface ToolbarViewInterface {
  /**
   * Navigates to {@link TheaterActivity}.
   */
  void navigateToTheater();

  /**
   * Navigates to {@link StudioActivity}.
   */
  void navigateToStudio();

  /**
   * Navigates to {@link RepertoireActivity}.
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
