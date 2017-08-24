package com.truethat.android.viewmodel;

import android.content.Context;
import android.content.res.Resources;
import com.truethat.android.R;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */
public class StudioViewModelTest extends ViewModelTestSuite {
  private StudioViewModel mViewModel;
  private StudioViewModel.DirectingState mCurrentState;
  private boolean mPublishedToBackend;

  @Before public void setUp() throws Exception {
    super.setUp();
    mCurrentState = DIRECTING;
    mPublishedToBackend = false;
    Context mockedContext = mock(Context.class);
    Resources mockedResources = mock(Resources.class);
    when(mockedContext.getResources()).thenReturn(mockedResources);
    when(mockedResources.getString(R.string.sent_failed)).thenReturn("TEST FAILED");
    mViewModel = createViewModel(StudioViewModel.class, (StudioViewInterface) new ViewInterface());
    mViewModel.setContext(mockedContext);
    mViewModel.onStart();
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        mPublishedToBackend = true;
        return new MockResponse().setBody("{\"type\":\"Pose\"}");
      }
    });
  }

  @Test public void directingState() throws Exception {
    assertDirectingState();
  }

  @Test public void approvalState() throws Exception {
    mViewModel.onApproval();
    assertApprovalState();
  }

  @Test public void approvalCancel() throws Exception {
    mViewModel.onApproval();
    assertApprovalState();
    // Cancel the picture taken
    mViewModel.disapprove();
    assertDirectingState();
  }

  @Test public void sentState() throws Exception {
    mViewModel.onApproval();
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
  }

  @Test public void publishedState() throws Exception {
    mViewModel.onApproval();
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
    mViewModel.onApproval();
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
    // Should fail
    assertPublishFailed();
    assertTrue(mPublishedToBackend);
  }

  @Test public void activityPausedWhileSending() throws Exception {
    mViewModel.onApproval();
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
        assertEquals(PUBLISHED, mCurrentState);
      }
    });
  }

  private void assertPublishFailed() {
    // Should return to approval.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertApprovalState();
      }
    });
  }

  private class ViewInterface extends UnitTestViewInterface implements StudioViewInterface {
    @Override public void onPublished() {
      mCurrentState = PUBLISHED;
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
  }
}