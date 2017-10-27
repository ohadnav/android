package com.truethat.android.viewmodel;

import com.truethat.android.viewmodel.viewinterface.OnBoardingCheeseStageViewInterface;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 27/10/2017 for TrueThat.
 */
public class OnBoardingCheeseStageViewModelTest extends ViewModelTestSuite {
  private OnBoardingCheeseStageViewModel mViewModel;
  private OnBoardingCheeseStageViewModelTest.ViewInterface mView;
  private long mOriginalDetectionDelay = OnBoardingCheeseStageViewModel.sDetectionDelay;
  private long mOriginalFaceLostTimeout = OnBoardingCheeseStageViewModel.sFaceLostTimeout;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Shorten delays
    OnBoardingCheeseStageViewModel.sDetectionDelay = 10;
    OnBoardingCheeseStageViewModel.sFaceLostTimeout = 10;
    // Initializing view model and its view interface.
    mView = new OnBoardingCheeseStageViewModelTest.ViewInterface();
    mViewModel = createViewModel(OnBoardingCheeseStageViewModel.class,
        (OnBoardingCheeseStageViewInterface) mView, null);
    // Starting view model.
    mViewModel.onStart();
    mViewModel.onResume();
    mViewModel.onVisible();
  }

  @Override public void tearDown() throws Exception {
    super.tearDown();
    // Restores original delays.
    OnBoardingCheeseStageViewModel.sDetectionDelay = mOriginalDetectionDelay;
    OnBoardingCheeseStageViewModel.sFaceLostTimeout = mOriginalFaceLostTimeout;
  }

  @Test public void completeStage() throws Exception {
    // Should show the "say cheese" layout.
    assertTrue(mView.mCheeseLayoutShown);
    assertFalse(mView.mLostFaceLayoutShown);
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect the necessary reaction.
    mFakeReactionDetectionManager.onReactionDetected(
        OnBoardingCheeseStageViewModel.REACTION_FOR_DONE, false);
    // Should complete stage (even though the detected reaction was not the most likely one).
    assertTrue(mView.mStageFinished);
  }

  @Test public void faceLost_afterVisible() throws Exception {
    // Should show the "say cheese" layout.
    assertTrue(mView.mCheeseLayoutShown);
    assertFalse(mView.mLostFaceLayoutShown);
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Lose face
    mFakeReactionDetectionManager.onFaceDetectionStopped();
    // Should not show face lost layout immediately
    assertTrue(mView.mCheeseLayoutShown);
    assertFalse(mView.mLostFaceLayoutShown);
    // Should show the lost face layout eventually
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(mView.mCheeseLayoutShown);
        assertTrue(mView.mLostFaceLayoutShown);
      }
    });
    // Detect face again
    mFakeReactionDetectionManager.onFaceDetectionStarted();
    // Resume to the "say cheese" layout.
    assertTrue(mView.mCheeseLayoutShown);
    assertFalse(mView.mLostFaceLayoutShown);
  }

  @Test public void faceLost_beforeDetection() throws Exception {
    // Lose face
    mFakeReactionDetectionManager.onFaceDetectionStopped();
    // Should show the "say cheese" layout.
    assertTrue(mView.mCheeseLayoutShown);
    assertFalse(mView.mLostFaceLayoutShown);
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Should show the lost face layout eventually
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(mView.mCheeseLayoutShown);
        assertTrue(mView.mLostFaceLayoutShown);
      }
    });
  }

  private class ViewInterface extends UnitTestViewInterface
      implements OnBoardingCheeseStageViewInterface {

    Boolean mCheeseLayoutShown;
    Boolean mLostFaceLayoutShown;
    Boolean mStageFinished;

    @Override public void showLostFaceLayout() {
      mLostFaceLayoutShown = true;
      mCheeseLayoutShown = false;
    }

    @Override public void showCheeseLayout() {
      mCheeseLayoutShown = true;
      mLostFaceLayoutShown = false;
    }

    @Override public void finishStage() {
      mStageFinished = true;
    }
  }
}