package com.truethat.android.viewmodel;

import android.content.Context;
import android.media.Image;
import com.truethat.android.R;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Media;
import com.truethat.android.model.Video;
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
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.CAMERA;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.EDIT;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.PUBLISHED;
import static com.truethat.android.viewmodel.StudioViewModel.DirectingState.SENT;
import static com.truethat.android.viewmodel.StudioViewModel.RECORD_RESOURCE;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
  private int mImageContent = 0;

  @Before public void setUp() throws Exception {
    super.setUp();

    // Create context
    Context mockedContext = mock(Context.class);
    when(mockedContext.getString(R.string.sent_failed)).thenReturn(SENT_FAILED);
    when(mockedContext.getString(R.string.saved_successfully)).thenReturn(SAVED_SUCCESSFULLY);
    // Start backend
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(mViewModel.getDirectedScene()));
      }
    });
    // Creates and starts view model
    mView = new ViewInterface();
    mViewModel = createViewModel(StudioViewModel.class, (StudioViewInterface) mView);
    mViewModel.setContext(mockedContext);
    mViewModel.onStart();
  }

  @Test public void directingState() throws Exception {
    assertCameraState();
  }

  @Test public void approvalState() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
  }

  @Test public void basicInteractiveScene() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Chose a follow up reaction
    mViewModel.onReactionChosen(Emotion.DISGUST);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Expect a flow tree with two nodes
    assertEquals(2, mViewModel.getDirectedScene().getMediaNodes().size());
    assertNotNull(mViewModel.getDirectedScene()
        .getNextMedia(mViewModel.getDirectedScene().getRootMedia(), Emotion.DISGUST));
    // Send the scene
    mViewModel.onSent();
    assertSentState();
  }

  @Test public void deepInteractiveScene() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Chose a follow up reaction
    mViewModel.onReactionChosen(Emotion.DISGUST);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Chose a follow up reaction
    mViewModel.onReactionChosen(Emotion.HAPPY);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Go to root media
    mViewModel.displayParentMedia();
    mViewModel.displayParentMedia();
    assertEquals(mViewModel.getDirectedScene().getRootMedia(), mViewModel.getCurrentMedia());
    // Chose a different follow up reaction
    mViewModel.onReactionChosen(Emotion.FEAR);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Return to root media and chose first reaction
    mViewModel.displayParentMedia();
    mViewModel.onReactionChosen(Emotion.DISGUST);
    assertEditState();
    // Chose a new reaction
    mViewModel.onReactionChosen(Emotion.SURPRISE);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    // Verify flow tree
    assertEquals(5, mViewModel.getDirectedScene().getMediaNodes().size());
    assertNotNull(mViewModel.getDirectedScene()
        .getNextMedia(mViewModel.getDirectedScene()
                .getNextMedia(mViewModel.getDirectedScene().getRootMedia(), Emotion.DISGUST),
            Emotion.HAPPY));
    assertNotNull(mViewModel.getDirectedScene()
        .getNextMedia(mViewModel.getDirectedScene()
                .getNextMedia(mViewModel.getDirectedScene().getRootMedia(), Emotion.DISGUST),
            Emotion.SURPRISE));
    assertNotNull(mViewModel.getDirectedScene()
        .getNextMedia(mViewModel.getDirectedScene().getRootMedia(), Emotion.FEAR));
  }

  @Test public void previousMedia() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Chose a follow up reaction
    mViewModel.onReactionChosen(Emotion.DISGUST);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Go to root media
    mViewModel.displayParentMedia();
    // Should edit root media
    assertEditState();
    assertEquals(mViewModel.getDirectedScene().getRootMedia(), mViewModel.getCurrentMedia());
  }

  @Test public void previousMediaHiddenFromRoot() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    assertFalse(mViewModel.mPreviousMediaVisibility.get());
  }

  @Test public void cancelNestedMedia() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    assertEquals(1, mViewModel.getDirectedScene().getMediaNodes().size());
    // Chose a follow up reaction
    mViewModel.onReactionChosen(Emotion.DISGUST);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    assertEquals(2, mViewModel.getDirectedScene().getMediaNodes().size());
    // Chose a nested follow up reaction
    mViewModel.onReactionChosen(Emotion.HAPPY);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    assertEquals(3, mViewModel.getDirectedScene().getMediaNodes().size());
    // Cancel last media
    mViewModel.disapprove();
    assertEditState();
    // Should have one node
    assertEquals(2, mViewModel.getDirectedScene().getMediaNodes().size());
  }

  @Test public void cancelRootMedia() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Chose a follow up reaction
    mViewModel.onReactionChosen(Emotion.DISGUST);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Chose a nested follow up reaction
    mViewModel.onReactionChosen(Emotion.HAPPY);
    assertCameraState();
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    assertEquals(3, mViewModel.getDirectedScene().getMediaNodes().size());
    // Go to root media
    mViewModel.displayParentMedia();
    mViewModel.displayParentMedia();
    assertEquals(mViewModel.getDirectedScene().getRootMedia(), mViewModel.getCurrentMedia());
    // Cancel root media
    mViewModel.disapprove();
    assertCameraState();
    // Should have a null scene
    assertNull(mViewModel.getDirectedScene());
  }

  @Test public void recordVideo() throws Exception {
    mViewModel.onVideoRecordStart();
    assertEquals(RECORD_RESOURCE, mViewModel.mCaptureButtonDrawableResource.get());
    mViewModel.onVideoRecorded("bigcoin-gen.dmg");
    assertEquals(CAPTURE_RESOURCE, mViewModel.mCaptureButtonDrawableResource.get());
    assertTrue(mViewModel.getDirectedScene().getRootMedia() instanceof Video);
  }

  @Test public void approvalCancel() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Cancel the picture taken
    mViewModel.disapprove();
    assertCameraState();
    // Should restore preview
    assertTrue(mView.mPreviewRestored);
  }

  @Test public void sentState() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Send the scene.
    mViewModel.onSent();
    assertSentState();
  }

  @Test public void publishedState() throws Exception {
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Send the scene.
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
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Send the scene.
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
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(mViewModel.getDirectedScene()));
      }
    });
    mViewModel.onPhotoTaken(createMockedImage());
    assertEditState();
    // Send the scene.
    mViewModel.onSent();
    assertSentState();
    // Pause activity.
    mViewModel.onStop();
    // Should fail
    assertPublishFailed();
  }

  private Image createMockedImage() {
    Image mockedImage = mock(Image.class);
    Image.Plane mockedPlane = mock(Image.Plane.class);
    when(mockedPlane.getBuffer()).thenReturn(ByteBuffer.wrap(new byte[] { (byte) mImageContent }));
    mImageContent++;
    when(mockedImage.getPlanes()).thenReturn(new Image.Plane[] { mockedPlane });
    return mockedImage;
  }

  private void assertCameraState() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        // Should communicate state via view interface.
        assertEquals(CAMERA, mViewModel.getState());
      }
    });
    // Capture buttons are displayed.
    assertTrue(mViewModel.mCaptureButtonVisibility.get());
    assertTrue(mViewModel.mSwitchCameraButtonVisibility.get());
    // Approval buttons are hidden
    assertFalse(mViewModel.mSendButtonVisibility.get());
    assertFalse(mViewModel.mCancelButtonVisibility.get());
    // Loading image is hidden
    assertFalse(mViewModel.mLoadingImageVisibility.get());
    // Scene preview is hidden.
    assertFalse(mViewModel.mScenePreviewVisibility.get());
    // Camera preview is shown
    assertTrue(mViewModel.mCameraPreviewVisibility.get());
    // Assert that if directed scene is not null then new edge is not null.
    if (mViewModel.getDirectedScene() != null) {
      assertNotNull(mViewModel.getChosenReaction());
    }
    assertNull(mView.mDisplayedMedia);
  }

  private void assertEditState() {
    // Should communicate state via view interface.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(EDIT, mViewModel.getState());
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
    assertTrue(mViewModel.mScenePreviewVisibility.get());
    // Camera preview is hidden
    assertFalse(mViewModel.mCameraPreviewVisibility.get());
    assertNotNull(mView.mDisplayedMedia);
    assertEquals(mViewModel.getCurrentMedia(), mView.mDisplayedMedia);
    // Previous media visible if not editing root media
    assertEquals(!mViewModel.getDirectedScene().getRootMedia().equals(mViewModel.getCurrentMedia()),
        mViewModel.mPreviousMediaVisibility.get());
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
    assertEquals(SENT, mViewModel.getState());
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mMockWebServer.getRequestCount());
      }
    });
  }

  private void assertPublishedState() {
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(PUBLISHED, mViewModel.getState());
        assertEquals(SAVED_SUCCESSFULLY, mView.getToastText());
        // Should leave studio
        assertTrue(mView.mLeftStudio);
      }
    });
  }

  private void assertPublishFailed() {
    // Should return to approval.
    assertEditState();
  }

  private class ViewInterface extends UnitTestViewInterface implements StudioViewInterface {
    private boolean mLeftStudio = false;
    private boolean mPreviewRestored = false;
    private Media mDisplayedMedia;

    @Override public void leaveStudio() {
      mLeftStudio = true;
    }

    @Override public void restoreCameraPreview() {
      mPreviewRestored = true;
    }

    @Override public void displayMedia(Media media) {
      mDisplayedMedia = media;
    }

    @Override public void removeMedia() {
      mDisplayedMedia = null;
    }
  }
}