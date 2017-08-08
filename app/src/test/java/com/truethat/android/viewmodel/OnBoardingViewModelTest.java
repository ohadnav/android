package com.truethat.android.viewmodel;

import com.truethat.android.application.AppContainer;
import com.truethat.android.viewmodel.viewinterface.OnBoardingViewInterface;
import java.util.concurrent.Callable;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */
public class OnBoardingViewModelTest extends ViewModelTestSuite {
  private static final String NAME = "Matt Damon";
  
  private OnBoardingViewModel mViewModel;
  private OnBoardingViewModelTest.ViewInterface mView;

  @Before public void setUp() throws Exception {
    super.setUp();
    mView = new OnBoardingViewModelTest.ViewInterface();
    mViewModel = createViewModel(OnBoardingViewModel.class, (OnBoardingViewInterface) mView);
    AppContainer.getReactionDetectionManager().start(null);
  }

  @Test public void successfulOnBoarding() throws Exception {
    // EditText should be auto focused.
    doOnBoarding(NAME);
    assertOnBoardingSuccessful();
  }

  @Test public void finalStage() throws Exception {
    // Type user name.
    mViewModel.mNameEditText.set(NAME);
    // Hit done
    mViewModel.onNameDone();
    // Wait for detection to start
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mFakeReactionDetectionManager.isDetecting();
      }
    });
    // Expose completion texts
    assertTrue(mViewModel.mCompletionTextVisibility.get());
    assertTrue(mViewModel.mCompletionSubscriptTextVisibility.get());
  }

  @Test public void typingName() throws Exception {
    // Type first name
    mViewModel.mNameEditText.set(NAME.split(" ")[0]);
    // Cursor should be visible.
    assertTrue(mViewModel.mNameEditCursorVisibility.get());
    assertInvalidName();
    // Lose focus, keyboard and cursor should be hidden.
    mViewModel.onNameFocusChange(false);
    assertFalse(mView.mKeyboardVisibility);
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
    // Type again, and assert cursor is visible again.
    mViewModel.onNameFocusChange(true);
    mViewModel.mNameEditText.set(NAME.split(" ")[0] + " ");
    assertTrue(mViewModel.mNameEditCursorVisibility.get());
    assertTrue(mView.mKeyboardVisibility);
    // Hit done
    mViewModel.onNameDone();
    // Should not be moving to next stage
    assertInvalidName();
    // Detect the final reaction.
    mFakeReactionDetectionManager.doDetection(OnBoardingViewModel.REACTION_FOR_DONE);
    // Should not be moving to next stage
    assertInvalidName();
    // Cursor and keyboard should be hidden.
    assertFalse(mView.mKeyboardVisibility);
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
    // Warning text visible.
    assertTrue(mViewModel.mWarningTextVisibility.get());
    // Type last name
    mViewModel.mNameEditText.set(NAME);
    assertValidName();
    // Hid done
    mViewModel.onNameDone();
    assertFinalStage();
    // Cursor should be hidden after hitting ime button.
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
  }

  /**
   * Programmatically completes the on boarding process as if a user completed it.
   *
   * @param name of the new user.
   */
  private void doOnBoarding(String name) {
    // Type name
    mViewModel.mNameEditText.set(name);
    // Hit done
    mViewModel.onNameDone();
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mFakeReactionDetectionManager.isDetecting();
      }
    });
    // Detect smile.
    mFakeReactionDetectionManager.doDetection(OnBoardingViewModel.REACTION_FOR_DONE);
    // Wait until Auth OK.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeAuthManager.isAuthOk());
      }
    });
  }

  private void assertOnBoardingSuccessful() {
    // On boarding should be finished.
    assertTrue(mView.mFinished);
  }

  private void assertInvalidName() {
    assertEquals(OnBoardingViewModel.ERROR_COLOR, mViewModel.mNameEditBackgroundTintColor.get());
    // Let detection start
    try {
      Thread.sleep(10);
    } catch (Exception e) {
      assertTrue(false);
    }
    // Should not be subscribed for reaction detected.
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Completion text is hidden
    assertFalse(mViewModel.mCompletionTextVisibility.get());
    assertFalse(mViewModel.mCompletionSubscriptTextVisibility.get());
  }

  private void assertValidName() {
    assertEquals(OnBoardingViewModel.VALID_NAME_COLOR, mViewModel.mNameEditBackgroundTintColor.get());
  }

  private void assertFinalStage() {
    // Completion text is hidden
    assertTrue(mViewModel.mCompletionTextVisibility.get());
    assertTrue(mViewModel.mCompletionSubscriptTextVisibility.get());
    // Warning text should be hidden.
    assertFalse(mViewModel.mWarningTextVisibility.get());
    // Assert detection is ongoing.
    assertTrue(mFakeReactionDetectionManager.isDetecting());
    assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Keyboard should be hidden.
    assertFalse(mView.mKeyboardVisibility);
  }

  private class ViewInterface extends UnitTestViewInterface implements OnBoardingViewInterface {
    private boolean mFinished = false;
    private boolean mKeyboardVisibility = false;

    @Override public void requestNameEditFocus() {
    }

    @Override public void hideSoftKeyboard() {
      mKeyboardVisibility = false;
    }

    @Override public void showSoftKeyboard() {
      mKeyboardVisibility = true;
    }

    @Override public void finishOnBoarding() {
      mFinished = true;
    }
  }
}