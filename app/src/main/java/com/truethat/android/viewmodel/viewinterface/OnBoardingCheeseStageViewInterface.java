package com.truethat.android.viewmodel.viewinterface;

import com.truethat.android.view.fragment.OnBoardingCheeseStageFragment;
import com.truethat.android.viewmodel.OnBoardingCheeseStageViewModel;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */

public interface OnBoardingCheeseStageViewInterface extends BaseFragmentViewInterface {
  /**
   * Indicates to the user that his face was lost in the detection.
   */
  void showLostFaceLayout();

  /**
   * Resumes to the normal state, where waiting for the user to react with {@link
   * OnBoardingCheeseStageViewModel#REACTION_FOR_DONE}.
   */
  void showCheeseLayout();

  /**
   * Invokes {@link OnBoardingCheeseStageFragment#bounceHappyEmojisIn(Runnable)} and finished on
   * boarding stage.
   */
  void finishStage();
}
