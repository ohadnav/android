package com.truethat.android.viewmodel;

import android.content.Context;
import android.media.Image;
import com.truethat.android.R;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Short;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Test;

import static com.truethat.android.viewmodel.StudioViewModel.CAPTURE_RESOURCE;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.APPROVAL;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.DIRECTING;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.PUBLISHED;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.SENT;
import static com.truethat.android.viewmodel.StudioViewModel.RECORD_RESOURCE;
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
  private static final String SENT_FAILED = "Sent failed";
  private static final String SAVED_SUCCESSFULLY = "Saved successfully";
  private StudioViewModel mViewModel;
  private ViewInterface mView;
  private Image mMockedImage;

  @Before public void setUp() throws Exception {
    super.setUp();

    // Create context
    Context mockedContext = mock(Context.class);
    when(mockedContext.getString(R.string.sent_failed)).thenReturn(SENT_FAILED);
    when(mockedContext.getString(R.string.saved_successfully)).thenReturn(SAVED_SUCCESSFULLY);
    // Start backend
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setBody(
            NetworkUtil.GSON.toJson(mViewModel.getDirectedReactable()));
      }
    });
    // Mocks take images
    mMockedImage = mock(Image.class);
    Image.Plane mockedPlane = mock(Image.Plane.class);
    when(mockedPlane.getBuffer()).thenReturn(ByteBuffer.wrap(new byte[] {}));
    when(mMockedImage.getPlanes()).thenReturn(new Image.Plane[] { mockedPlane });
    // Creates and starts view model
    mView = new ViewInterface();
    mViewModel = createViewModel(StudioViewModel.class, (StudioViewInterface) mView);
    mViewModel.setContext(mockedContext);
    mViewModel.onStart();
  }

  @Test public void directingState() throws Exception {
    assertDirectingState();
  }

  @Test public void approvalState() throws Exception {
    mViewModel.onImageAvailable(mMockedImage);
    assertApprovalState();
  }

  @Test public void recordVideo() throws Exception {
    mViewModel.onVideoRecordStart();
    assertEquals(RECORD_RESOURCE, mViewModel.mCaptureButtonDrawableResource.get());
    mViewModel.onVideoAvailable("bigcoin-gen.dmg");
    assertEquals(CAPTURE_RESOURCE, mViewModel.mCaptureButtonDrawableResource.get());
    assertTrue(mViewModel.getDirectedReactable() instanceof Short);
  }

  @Test public void approvalCancel() throws Exception {
    mViewModel.onImageAvailable(mMockedImage);
    assertApprovalState();
    // Cancel the picture taken
    mViewModel.disapprove();
    assertDirectingState();
    // Should restore preview
    assertTrue(mView.mPreviewRestored);
  }

  @Test public void sentState() throws Exception {
    mViewModel.onImageAvailable(mMockedImage);
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
  }

  @Test public void publishedState() throws Exception {
    mViewModel.onImageAvailable(mMockedImage);
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
    assertPublishedState();
  }

  @Test public void publishedFailed() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    mViewModel.onImageAvailable(mMockedImage);
    assertApprovalState();
    // Send the reactable.
    mViewModel.onSent();
    assertSentState();
    // Should fail
    assertPublishFailed();
    assertEquals(SENT_FAILED, mView.getToastText());
  }

  @Test public void activityPausedWhileSending() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        Thread.sleep(500);
        return new MockResponse().setBody(
            NetworkUtil.GSON.toJson(mViewModel.getDirectedReactable()));
      }
    });
    mViewModel.onImageAvailable(mMockedImage);
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
    assertEquals(DIRECTING, mViewModel.getDirectingState());
    // Reactable preview is hidden.
    assertFalse(mViewModel.mReactablePreviewVisibility.get());
    // Camera preview is shown
    assertTrue(mViewModel.mCameraPreviewVisibility.get());
  }

  private void assertApprovalState() {
    // Should communicate state via view interface.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(APPROVAL, mViewModel.getDirectingState());
      }
    });
    // Capture buttons are hidden.
    assertFalse(mViewModel.mCaptureButtonVisibility.get());
    assertFalse(mViewModel.mSwitchCameraButtonVisibility.get());
    // Approval buttons are shown
    assertTrue(mViewModel.mSendButtonVisibility.get());
    assertTrue(mViewModel.mCancelButtonVisibility.get());
    // Loading image is hidden
    assertFalse(mViewModel.mLoadingImageVisibility.get());
    // Correct preview is shown.
    assertTrue(mViewModel.mReactablePreviewVisibility.get());
    // Camera preview is hidden
    assertFalse(mViewModel.mCameraPreviewVisibility.get());
    assertEquals(mViewModel.getDirectedReactable(), mView.mDisplayedReactable);
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
    assertEquals(SENT, mViewModel.getDirectingState());
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mMockWebServer.getRequestCount());
      }
    });
  }

  private void assertPublishedState() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(PUBLISHED, mViewModel.getDirectingState());
        assertEquals(SAVED_SUCCESSFULLY, mView.getToastText());
        // Should leave studio
        assertTrue(mView.mLeftStudio);
      }
    });
  }

  private void assertPublishFailed() {
    // Should return to approval.
    assertApprovalState();
  }

  private class ViewInterface extends UnitTestViewInterface implements StudioViewInterface {
    private boolean mLeftStudio = false;
    private boolean mPreviewRestored = false;
    private Reactable mDisplayedReactable;

    @Override public void leaveStudio() {
      mLeftStudio = true;
    }

    @Override public void restoreCameraPreview() {
      mPreviewRestored = true;
    }

    @Override public void displayPreview(Reactable reactable) {
      mDisplayedReactable = reactable;
    }
  }
}