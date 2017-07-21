package com.truethat.android.viewmodel;

import android.support.annotation.Nullable;
import com.truethat.android.model.Scene;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.net.HttpURLConnection;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;

import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.APPROVAL;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.DIRECTING;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.PUBLISHED;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.SENT;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */
public class StudioViewModelTest extends ViewModelTestSuite {
  private StudioViewModel mViewModel;
  private StudioViewInterface mViewInterface;
  private StudioViewModel.DirectingState mCurrentState;
  private boolean mHadPublishedState;
  private boolean mPublishedToBackend;

  @Before public void setUp() throws Exception {
    super.setUp();
    mCurrentState = DIRECTING;
    mHadPublishedState = false;
    mPublishedToBackend = false;
    mViewInterface = new StudioViewInterface() {
      @Override public void onPublished() {
        mCurrentState = PUBLISHED;
        mHadPublishedState = true;
      }

      @Override public void onApproval() {
        mCurrentState = APPROVAL;
      }

      @Override public void onSent() {
        mCurrentState = SENT;
      }

      @Override public void onDirecting() {
        mCurrentState = DIRECTING;
      }

      @Override public void toast(String text) {

      }

      @Override public void onAuthOk() {

      }

      @Override public void onAuthFailed() {

      }

      @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
        return null;
      }

      @Override public void removeViewModel() {

      }
    };
    mViewModel = createViewModel(StudioViewModel.class, mViewInterface);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mPublishedToBackend = true;
        return new MockResponse().setBody("{\"type\":\"Scene\"}");
      }
    });
  }

  @Test public void directingState() throws Exception {
    assertDirectingState();
  }

  @Test public void approvalState() throws Exception {
    mViewModel.onApproval(new Scene(new byte[1]));
    assertApprovalState();
  }

  @Test public void approvalCancel() throws Exception {
    mViewModel.onApproval(new Scene(new byte[1]));
    assertApprovalState();
    // Cancel the picture taken
    mViewModel.disapprove();
    assertDirectingState();
  }

  @Test public void sentState() throws Exception {
    mViewModel.onApproval(new Scene(new byte[1]));
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
  }

  @Test public void publishedState() throws Exception {
    mViewModel.onApproval(new Scene(new byte[1]));
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
    assertPublishedState();
  }

  @Test public void publishedFailed() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mPublishedToBackend = true;
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    mViewModel.onApproval(new Scene(new byte[1]));
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
    // Should fail
    assertPublishFailed();
    assertTrue(mPublishedToBackend);
  }

  @Test public void activityPausedWhileSending() throws Exception {
    mViewModel.onApproval(new Scene(new byte[1]));
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
    // Pause activity.
    mViewModel.onStop();
    // Should fail
    assertPublishFailed();
  }

  private void assertDirectingState() {
    // Capture buttons are displayed.
    assertTrue(mViewModel.mCaptureButtonVisibility.get());
    assertTrue(mViewModel.mSwitchCameraButtonVisibility.get());
    // Approval buttons are hidden
    assertFalse(mViewModel.mSendButtonVisibility.get());
    assertFalse(mViewModel.mCancelButtonVisibility.get());
    // Loading image is hidden
    assertFalse(mViewModel.mLoadingImageVisibility.get());
    // Should communicate state via view interface.
    assertEquals(DIRECTING, mCurrentState);
  }

  private void assertApprovalState() {
    // Capture buttons are hidden.
    assertFalse(mViewModel.mCaptureButtonVisibility.get());
    assertFalse(mViewModel.mSwitchCameraButtonVisibility.get());
    // Approval buttons are shown
    assertTrue(mViewModel.mSendButtonVisibility.get());
    assertTrue(mViewModel.mCancelButtonVisibility.get());
    // Loading image is hidden
    assertFalse(mViewModel.mLoadingImageVisibility.get());
    // Should communicate state via view interface.
    assertEquals(APPROVAL, mCurrentState);
  }

  private void assertSentState() {
    // Capture buttons are hidden.
    assertFalse(mViewModel.mCaptureButtonVisibility.get());
    assertFalse(mViewModel.mSwitchCameraButtonVisibility.get());
    // Approval buttons are hidden
    assertFalse(mViewModel.mSendButtonVisibility.get());
    assertFalse(mViewModel.mCancelButtonVisibility.get());
    // Loading image is shown
    assertTrue(mViewModel.mLoadingImageVisibility.get());
    // Should communicate state via view interface.
    assertEquals(SENT, mCurrentState);
  }

  private void assertPublishedState() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mPublishedToBackend);
      }
    });
    assertTrue(mHadPublishedState);
  }

  private void assertPublishFailed() {
    assertFalse(mHadPublishedState);
    // Should return to approval.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertApprovalState();
      }
    });
  }
}