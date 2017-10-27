package com.truethat.android.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.truethat.android.R;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.application.auth.AuthResult;
import com.truethat.android.common.util.StringUtil;
import com.truethat.android.viewmodel.viewinterface.OnBoardingSignUpStageViewInterface;
import java.net.HttpURLConnection;
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
public class OnBoardingSignUpStageViewModelTest extends ViewModelTestSuite {
  private static final String NAME = "Matt Damon";

  private OnBoardingSignUpStageViewModel mViewModel;
  private OnBoardingSignUpStageViewModelTest.ViewInterface mView;
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
    mViewModel.onHidden();
    assertTrue(mFakeAuthManager.getAuthCall().isCanceled());
    // Should stop reaction detection
    assertFalse(mFakeReactionDetectionManager.isDetecting());
    // Starting view model again should resume to final stage.
    mViewModel.onVisible();
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
    // Show dialog
    assertTrue(mView.mDialogShown);
    // Should not complete on boarding
    assertFalse(mFinishedOnBoarding);
  }

  @Test public void editStage() throws Exception {
    // Should be in edit stage
    assertEquals(OnBoardingSignUpStageViewModel.Stage.EDIT, mViewModel.getStage());
    // Input type should be person name
    assertEquals(OnBoardingSignUpStageViewModel.NAME_TEXT_EDITING_INPUT_TYPE,
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
    // Click sign up
    mViewModel.doSignUp();
    // Should not be moving to next stage
    assertInvalidName();
    // Cursor and keyboard should be hidden.
    assertFalse(mView.isKeyboardVisible());
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
    // Warning text visible.
    assertTrue(mViewModel.mWarningTextVisibility.get());
    // Type last name
    mViewModel.mNameEditText.set(NAME);
    assertValidName();
    // Hid done
    mViewModel.onNameDone();
    assertNameDone();
    // Cursor should be hidden after hitting ime button.
    assertFalse(mViewModel.mNameEditCursorVisibility.get());
  }

  private void initViewModel(@Nullable Bundle savedInstanceState) throws Exception {
    // Initializing view model and its view interface.
    mView = new ViewInterface();
    mViewModel = createViewModel(OnBoardingSignUpStageViewModel.class,
        (OnBoardingSignUpStageViewInterface) mView,
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
    mViewModel.onVisible();
  }

  /**
   * Programmatically completes the on boarding process as if a user completed it.
   *
   */
  private void doOnBoarding() {
    // Type name
    mViewModel.mNameEditText.set(OnBoardingSignUpStageViewModelTest.NAME);
    // Hit done
    mViewModel.onNameDone();
  }

  private void assertInvalidName() {
    assertEquals(OnBoardingSignUpStageViewModel.ERROR_COLOR,
        mViewModel.mNameEditBackgroundTintColor.get());
  }

  private void assertValidName() {
    assertEquals(OnBoardingSignUpStageViewModel.VALID_COLOR,
        mViewModel.mNameEditBackgroundTintColor.get());
  }

  private void assertNameDone() {
    // Warning text should be hidden.
    assertFalse(mViewModel.mWarningTextVisibility.get());
    // Keyboard should be hidden.
    assertFalse(mView.isKeyboardVisible());
  }

  private void assertSentStage() {
    assertEquals(OnBoardingSignUpStageViewModel.Stage.REQUEST_SENT, mViewModel.getStage());
    // Should be sending an auth call.
    assertFalse(mFakeAuthManager.getAuthCall().isCanceled());
    // Loading should be visible
    assertTrue(mViewModel.mLoadingImageVisibility.get());
    // Input type should be disabled
    assertEquals(OnBoardingSignUpStageViewModel.DISABLED_INPUT_TYPE,
        mViewModel.mNameEditInputType.get());
  }

  private class ViewInterface extends UnitTestViewInterface
      implements OnBoardingSignUpStageViewInterface {
    private boolean mIsKeyboardVisible = false;
    private boolean mIsNameEditFocused = false;
    private boolean mDialogShown = false;

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

    @Override public void showFailedSignUpDialog() {
      mDialogShown = true;
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