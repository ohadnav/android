package com.truethat.android.viewmodel;

import android.support.annotation.VisibleForTesting;
import com.truethat.android.application.AppContainer;
import com.truethat.android.empathy.ReactionDetectionListener;
import com.truethat.android.model.Emotion;
import com.truethat.android.viewmodel.viewinterface.OnBoardingCheeseStageViewInterface;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Proudly created by ohad on 27/10/2017 for TrueThat.
 */

public class OnBoardingCheeseStageViewModel
    extends BaseFragmentViewModel<OnBoardingCheeseStageViewInterface>
    implements ReactionDetectionListener {
  @VisibleForTesting public static final Emotion REACTION_FOR_DONE = Emotion.HAPPY;
  @VisibleForTesting public static long sDetectionDelay = 500;
  @VisibleForTesting public static long sFaceLostTimeout = 2000;
  private Timer mLostFaceTimer;
  private Timer mDetectionTimer;

  @Override public void onVisible() {
    super.onVisible();
    // Starts detection.
    if (getView() != null) {
      mDetectionTimer = new Timer();
      mDetectionTimer.schedule(new TimerTask() {
        @Override public void run() {
          AppContainer.getReactionDetectionManager().start(getView().getBaseActivity());
          // Subscribes to reaction detection.
          AppContainer.getReactionDetectionManager().subscribe(OnBoardingCheeseStageViewModel.this);
        }
      }, sDetectionDelay);
      // Hides lost face layout
      getView().showCheeseLayout();
    }
  }

  @Override public void onHidden() {
    super.onHidden();
    killLostFaceTimer();
    killDetectionTimer();
    // Stops detection.
    AppContainer.getReactionDetectionManager().stop();
    // Unsubscribes from reaction detection.
    AppContainer.getReactionDetectionManager().unsubscribe(this);
  }

  @Override public void onFaceDetectionStarted() {
    killLostFaceTimer();
    if (getView() != null) {
      getView().showCheeseLayout();
    }
  }

  @Override public void onFaceDetectionStopped() {
    mLostFaceTimer = new Timer();
    mLostFaceTimer.schedule(new TimerTask() {
      @Override public void run() {
        if (getView() != null) {
          getView().showLostFaceLayout();
        }
      }
    }, sFaceLostTimeout);
  }

  @Override public void onReactionDetected(Emotion reaction, boolean mostLikely) {
    if (reaction == REACTION_FOR_DONE && getView() != null) {
      getView().finishStage();
      // Unsubscribes from reaction detection.
      AppContainer.getReactionDetectionManager().unsubscribe(this);
    }
  }

  private void killLostFaceTimer() {
    if (mLostFaceTimer != null) {
      mLostFaceTimer.cancel();
      mLostFaceTimer.purge();
      mLostFaceTimer = null;
    }
  }

  private void killDetectionTimer() {
    if (mDetectionTimer != null) {
      mDetectionTimer.cancel();
      mDetectionTimer.purge();
      mDetectionTimer = null;
    }
  }
}
