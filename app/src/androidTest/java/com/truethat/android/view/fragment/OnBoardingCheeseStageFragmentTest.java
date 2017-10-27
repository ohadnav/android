package com.truethat.android.view.fragment;

import com.truethat.android.R;
import com.truethat.android.common.util.StyleUtil;
import com.truethat.android.view.activity.BaseOnBoardingTest;
import com.truethat.android.view.activity.OnBoardingActivity;
import com.truethat.android.viewmodel.OnBoardingCheeseStageViewModel;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 27/10/2017 for TrueThat.
 */
public class OnBoardingCheeseStageFragmentTest extends BaseOnBoardingTest {

  @Test public void completeStage() throws Exception {
    manualSetUp();
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            mActivity.mCheeseStageFragment.getViewModel()));
      }
    });
    // Proceed to sign up stage
    mFakeReactionDetectionManager.onReactionDetected(
        OnBoardingCheeseStageViewModel.REACTION_FOR_DONE, true);
    // Wait for sign up stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.SIGN_UP_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
  }

  @Test public void faceLost() throws Exception {
    manualSetUp();
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            mActivity.mCheeseStageFragment.getViewModel()));
      }
    });
    // Lose face
    mFakeReactionDetectionManager.onFaceDetectionStopped();
    // Should show lost face layout
    await().atMost(Duration.TWO_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(mActivity.mCheeseStageFragment.mLostFaceLayout.getAlpha(), 1f, 0.01);
      }
    });
    // Should bounce blind emoji
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(mActivity.mCheeseStageFragment.mBlindEmoji.getScaleX(),
            StyleUtil.DEFAULT_SCALE, 0.01);
      }
    });
    // Detect face again
    mFakeReactionDetectionManager.onFaceDetectionStarted();
    // Should hide lost face layout
    await().atMost(Duration.TWO_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(mActivity.mCheeseStageFragment.mLostFaceLayout.getAlpha(), 0f, 0.01);
      }
    });
    // Should show say cheese title
    await().atMost(Duration.TWO_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(mActivity.mCheeseStageFragment.mCheeseLayout.getAlpha(), 1f, 0.01);
      }
    });
    // Proceed to sign up stage
    mFakeReactionDetectionManager.onReactionDetected(
        OnBoardingCheeseStageViewModel.REACTION_FOR_DONE, true);
    // Wait for sign up stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.SIGN_UP_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
  }

  @Test public void testStageSaved() throws Exception {
    manualSetUp();
    // Destroy activity and resume to it.
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mActivity.recreate();
      }
    });
    // Should resume to cheese state
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.CHEESE_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
  }

  @Override protected void manualSetUp() {
    super.manualSetUp();
    // Proceed to cheese stage
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        //noinspection ConstantConditions
        mActivity.mHiStageFragment.getView().findViewById(R.id.onBoarding_hiButton).performClick();
      }
    });
    // Wait for cheese stage
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingActivity.CHEESE_STAGE_INDEX, mActivity.getStageIndex());
      }
    });
  }
}