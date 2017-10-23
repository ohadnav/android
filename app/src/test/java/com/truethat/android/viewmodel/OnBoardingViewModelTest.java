package com.truethat.android.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.truethat.android.R;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.auth.AuthResult;
import com.truethat.android.common.util.StringUtil;
import com.truethat.android.viewmodel.viewinterface.OnBoardingViewInterface;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 31/07/2017 for TrueThat.
 */
public class OnBoardingViewModelTest extends ViewModelTestSuite {
  private static final String NAME = "Matt Damon";
  
  private OnBoardingViewModel mViewModel;
  private OnBoardingViewModelTest.ViewInterface mView;
  private boolean mFinishedOnBoarding;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Signs out.
    mFinishedOnBoarding = false;
    mFakeAuthManager.signOut(new AuthListener() {
      @Override public void onAuthOk() {

      }

      @Override public void onAuthFailed() {

      }

      @Override public String getTAG() {
        return "AnonymousAuthListener";
      }
    });
    initViewModel(null);
  }

  @Test public void successfulOnBoarding() throws Exception {
    doOnBoarding();
    // Wait until Auth OK.
    assertTrue(mFakeAuthManager.isAuthOk());
    assertEquals(StringUtil.toTitleCase(NAME), mFakeAuthManager.getCurrentUser().getDisplayName());
    assertEquals(AuthResult.OK, mView.getAuthResult());
    assertTrue(mFinishedOnBoarding);
  }

  @Test public void finalStage() throws Exception {
    // Type user name.
    mViewModel.mNameEditText.set(NAME);
    // Hit done
    mViewModel.onNameDone();
    assertFinalStage();
    // Stopping view model should unsubscribe reaction detection
    mViewModel.onPause();
    assertFalse(mFakeReactionDetectionManager.isDetecting());
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Starting view model should resume to final stage
    mViewModel.onResume();
    assertFinalStage();
  }

  @Test public void finalStage_nonPrevalentEmotion() throws Exception {
    // Type user name.
    mViewModel.mNameEditText.set(NAME);
    // Hit done
    mViewModel.onNameDone();
    assertFinalStage();
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mFakeReactionDetectionManager.isDetecting();
      }
    });
    // Detect smile.
    mFakeReactionDetectionManager.onReactionDetected(OnBoardingViewModel.REACTION_FOR_DONE, false);
    // Wait until Auth OK.
    assertTrue(mFakeAuthManager.isAuthOk());
  }

  @Test public void requestSentStage() throws Exception {
    mView = new ViewInterface() {
      @Override public void onAuthFailed() {
      }
    };
    initViewModel(null);
    mFakeAuthManager.useNetwork();
    doOnBoarding();
    // Should be in request sent stage
    assertSentStage();
    // Stopping view model, should cancel request
    mViewModel.onPause();
    assertTrue(mFakeAuthManager.getAuthCall().isCanceled());
    // Should stop reaction detection
    assertFalse(mFakeReactionDetectionManager.isDetecting());
    // Starting view model again should resume to final stage.
    mViewModel.onResume();
    assertSentStage();
  }

  @Test public void failedSignUp() throws Exception {
    mFakeAuthManager.useNetwork();
    // Set up server to fail.
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        Thread.sleep(ViewModelTestSuite.DEFAULT_TIMEOUT.getValueInMS() / 2);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    // Set up view interface to invoke failure method
    doOnBoarding();
    // Should be in request sent stage
    assertSentStage();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(AuthResult.FAILED, mView.getAuthResult());
      }
    });
    // Hide loading indicator
    assertFalse(mViewModel.mLoadingImageVisibility.get());
    // Show warning
    assertTrue(mViewModel.mWarningTextVisibility.get());
    // and change text
    assertEquals("sign_up_failed_warning_text", mViewModel.mWarningText.get());
    // Should not complete on boarding
    assertFalse(mFinishedOnBoarding);
  }

  @Test public void editStage() throws Exception {
    // Should be in edit stage
    assertEquals(OnBoardingViewModel.Stage.EDIT, mViewModel.getStage());
    // Input type should be person name
    assertEquals(OnBoardingViewModel.NAME_TEXT_EDITING_INPUT_TYPE,
        mViewModel.mNameEditInputType.get());
    // EditText should have focus
    assertTrue(mView.isNameEditFocused());
    // Type first name
    mViewModel.mNameEditText.set(NAME.split(" ")[0]);
    // Cursor should be visible.
    assertTrue(mViewModel.mNameEditCursorVisibility.get());
    assertInvalidName();
    // Lose focus, keyboard and cursor should be hidden.
    mViewModel.onNameFocusChange(false);
    mView.mIsNameEditFocused = false;
    assertFalse(mView.isKeyboardVisible());
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
    // Type again, and assert cursor is visible again.
    mViewModel.onNameFocusChange(true);
    mViewModel.mNameEditText.set(NAME.split(" ")[0] + " ");
    assertTrue(mViewModel.mNameEditCursorVisibility.get());
    assertTrue(mView.isKeyboardVisible());
    // Cannot test for name edit focus, but it should be assertTrue(mView.isNameEditFocused());
    // Hit done
    mViewModel.onNameDone();
    // Should not be moving to next stage
    assertInvalidName();
    // Detect the final reaction.
    mFakeReactionDetectionManager.onReactionDetected(OnBoardingViewModel.REACTION_FOR_DONE, true);
    // Should not be moving to next stage
    assertInvalidName();
    // Cursor and keyboard should be hidden.
    assertFalse(mView.isKeyboardVisible());
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
    // Warning text visible.
    assertTrue(mViewModel.mWarningTextVisibility.get());
    // and with correct text
    assertEquals("name_edit_warning_text", mViewModel.mWarningText.get());
    // Type last name
    mViewModel.mNameEditText.set(NAME);
    assertValidName();
    // Hid done
    mViewModel.onNameDone();
    assertFinalStage();
    // Cursor should be hidden after hitting ime button.
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
  }

  private void initViewModel(@Nullable Bundle savedInstanceState) throws Exception {
    // Initializing view model and its view interface.
    mView = new ViewInterface();
    mViewModel = createViewModel(OnBoardingViewModel.class, (OnBoardingViewInterface) mView,
        savedInstanceState);
    // Creating fake context
    Context mockedContext = mock(Context.class);
    when(mockedContext.getString(R.string.name_edit_warning_text)).thenReturn(
        "name_edit_warning_text");
    when(mockedContext.getString(R.string.sign_up_failed_warning_text)).thenReturn(
        "sign_up_failed_warning_text");
    mViewModel.setContext(mockedContext);
    // Starting view model.
    mViewModel.onStart();
    mViewModel.onResume();
  }

  /**
   * Programmatically completes the on boarding process as if a user completed it.
   *
   */
  private void doOnBoarding() {
    // Type name
    mViewModel.mNameEditText.set(OnBoardingViewModelTest.NAME);
    // Hit done
    mViewModel.onNameDone();
    assertFinalStage();
    // Wait until detection had started.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return mFakeReactionDetectionManager.isDetecting();
      }
    });
    // Detect smile.
    mFakeReactionDetectionManager.onReactionDetected(OnBoardingViewModel.REACTION_FOR_DONE, true);
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
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(OnBoardingViewModel.Stage.FINAL, mViewModel.getStage());
      }
    });
    // Completion text is hidden
    assertTrue(mViewModel.mCompletionTextVisibility.get());
    assertTrue(mViewModel.mCompletionSubscriptTextVisibility.get());
    // Warning text should be hidden.
    assertFalse(mViewModel.mWarningTextVisibility.get());
    // Assert detection is ongoing.
    assertTrue(mFakeReactionDetectionManager.isDetecting());
    assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Keyboard should be hidden.
    assertFalse(mView.isKeyboardVisible());
    // Detection should start
    assertTrue(mFakeReactionDetectionManager.isDetecting());
  }

  private void assertSentStage() {
    assertEquals(OnBoardingViewModel.Stage.REQUEST_SENT, mViewModel.getStage());
    // Should be sending an auth call.
    assertFalse(mFakeAuthManager.getAuthCall().isCanceled());
    // Loading should be visible
    assertTrue(mViewModel.mLoadingImageVisibility.get());
    // Should unsubscribe view model from reaction detection
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Input type should be disabled
    assertEquals(OnBoardingViewModel.NAME_TEXT_DISABLED_INPUT_TYPE,
        mViewModel.mNameEditInputType.get());
  }

  private class ViewInterface extends UnitTestViewInterface implements OnBoardingViewInterface {
    private boolean mIsKeyboardVisible = false;
    private boolean mIsNameEditFocused = false;

    @Override public void requestNameEditFocus() {
      mIsNameEditFocused = true;
    }

    @Override public void clearNameEditFocus() {
      mIsNameEditFocused = false;
    }

    @Override public void hideSoftKeyboard() {
      mIsKeyboardVisible = false;
    }

    @Override public void showSoftKeyboard() {
      mIsKeyboardVisible = true;
    }

    @Override public AuthListener getAuthListener() {
      return this;
    }

    @Override public void onAuthOk() {
      super.onAuthOk();
      mFinishedOnBoarding = true;
    }

    @Override public void onAuthFailed() {
      super.onAuthFailed();
      mViewModel.failedSignUp();
    }

    boolean isKeyboardVisible() {
      return mIsKeyboardVisible;
    }

    boolean isNameEditFocused() {
      return mIsNameEditFocused;
    }
  }
}