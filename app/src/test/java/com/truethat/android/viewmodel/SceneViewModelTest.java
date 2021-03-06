package com.truethat.android.viewmodel;

import android.support.annotation.NonNull;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.model.Edge;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.EventType;
import com.truethat.android.model.InteractionEvent;
import com.truethat.android.model.Media;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.viewinterface.SceneViewInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;
import org.awaitility.core.ThrowingRunnable;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */
@SuppressWarnings("serial") public class SceneViewModelTest extends ViewModelTestSuite {
  private static final long SCENE_ID = 1;
  private static final Media MEDIA_0 = new Photo(0L, "0");
  private static final Media MEDIA_1 = new Photo(1L, "1");
  private static final Media MEDIA_2 = new Photo(2L, "2");
  private static final User DIRECTOR = new User(FakeAuthManager.USER_ID + 1, "Avi", "Nimni");
  private static final TreeMap<Emotion, Long> REACTION_COUNTERS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, 100L);
    put(Emotion.OMG, 20000L);
  }};
  private static final Emotion REACTION = Emotion.HAPPY;
  private static final Emotion REACTION_2 = Emotion.OMG;
  private static final Edge EDGE_0_TO_1 = new Edge(MEDIA_0.getId(), MEDIA_1.getId(), REACTION);
  private static final Edge EDGE_0_TO_2 = new Edge(MEDIA_0.getId(), MEDIA_2.getId(), REACTION_2);
  private static final Edge EDGE_1_TO_2 = new Edge(MEDIA_1.getId(), MEDIA_2.getId(), REACTION_2);
  private SceneViewModel mViewModel = new SceneViewModel();
  private ViewInterface mView;
  private Scene mScene;

  @BeforeClass public static void beforeClass() throws Exception {
    SceneViewModel.setDetectionDelayMillis(10);
  }

  private static void assertSceneDisplayed(SceneViewModel viewModel, Scene scene,
      @NonNull User currentUser) {
    assertEquals(scene.getId(), viewModel.getScene().getId());
    // Counters should be summed up and abbreviated.
    assertEquals(NumberUtil.format(NumberUtil.sum(scene.getReactionCounters())),
        viewModel.mReactionsCountText.get());
    // Hide director name when ths current user is the director.
    assertNotEquals(currentUser.equals(scene.getDirector()),
        viewModel.mDirectorNameVisibility.get());
    assertEquals(scene.getDirector().getDisplayName(), viewModel.mDirectorName.get());
    // Display time ago in a proper fashion.
    assertEquals(DateUtil.formatTimeAgo(scene.getCreated()), viewModel.mTimeAgoText.get());
  }

  @Test public void basicDisplay() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, MEDIA_0);
    initSceneViewModel();
    assertSceneDisplayed(mViewModel, mScene, AppContainer.getAuthManager().getCurrentUser());
    assertEquals(SceneViewModel.DEFAULT_COUNT_COLOR, mViewModel.mReactionCountColor.get());
    assertEquals(MEDIA_0, mView.getDisplayedMedia());
    // By default, reactions layout should be faded
    assertNull(mView.reactionsLayoutExposed);
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    assertFalse(mView.reactionsLayoutExposed);
  }

  @Test public void faceDetectionOnGoing_beforeDisplay() throws Exception {
    mFakeReactionDetectionManager.onFaceDetectionStarted();
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, MEDIA_0);
    initSceneViewModel();
    assertSceneDisplayed(mViewModel, mScene, AppContainer.getAuthManager().getCurrentUser());
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Reactions layout should be exposed
    assertNotNull(mView.reactionsLayoutExposed);
    assertTrue(mView.reactionsLayoutExposed);
  }

  @Test public void faceDetectionOnGoing_whileDisplay() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, MEDIA_0);
    initSceneViewModel();
    assertSceneDisplayed(mViewModel, mScene, AppContainer.getAuthManager().getCurrentUser());
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Reactions layout should be faded
    assertFalse(mView.reactionsLayoutExposed);
    // Detect a face.
    mFakeReactionDetectionManager.onFaceDetectionStarted();
    // Reactions layout should be exposed
    assertTrue(mView.reactionsLayoutExposed);
  }

  @Test public void faceDetectionStopped_whileDisplay() throws Exception {
    mFakeReactionDetectionManager.onFaceDetectionStarted();
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, MEDIA_0);
    initSceneViewModel();
    assertSceneDisplayed(mViewModel, mScene, AppContainer.getAuthManager().getCurrentUser());
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Reactions layout should be exposed
    assertNotNull(mView.reactionsLayoutExposed);
    assertTrue(mView.reactionsLayoutExposed);
    // Face detection stopped.
    mFakeReactionDetectionManager.onFaceDetectionStopped();
    // Reactions layout should be now faded
    assertFalse(mView.reactionsLayoutExposed);
  }

  @Test public void displayZeroReactions() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, new TreeMap<Emotion, Long>(), mNow, MEDIA_0);
    initSceneViewModel();
    assertEquals("", mViewModel.mReactionsCountText.get());
    assertEquals(R.drawable.transparent_1x1, mViewModel.mReactionDrawableResource.get());
  }

  @Test public void basicNextMedia() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, Arrays.asList(MEDIA_0, MEDIA_1),
        Collections.singletonList(EDGE_0_TO_1));
    initSceneViewModel();
    assertEquals(MEDIA_0, mView.getDisplayedMedia());
    // Assert a view event is sent
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(EventType.VIEW,
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class).getEventType());
      }
    });
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a reaction
    mFakeReactionDetectionManager.onReactionDetected(EDGE_0_TO_1.getReaction(), true);
    // Wait for reaction event
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(EventType.REACTION,
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class).getEventType());
      }
    });
    // Should update next media
    assertEquals(MEDIA_1, mViewModel.getNextMedia());
    mView.finishMedia();
    // Should navigate to next media
    assertEquals(MEDIA_1, mView.getDisplayedMedia());
    // Should reset next media
    assertNull(mViewModel.getNextMedia());
    // Should stop reaction detection
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Wait for next view event
    mViewModel.onReady();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(EventType.VIEW,
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class).getEventType());
      }
    });
    // Should resume reaction detection
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
  }

  @Test public void nextMedia_correctMediaChosen() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow,
        Arrays.asList(MEDIA_0, MEDIA_1, MEDIA_2), Arrays.asList(EDGE_0_TO_1, EDGE_0_TO_2));
    initSceneViewModel();
    assertEquals(MEDIA_0, mView.getDisplayedMedia());
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Triggers navigation to next media
    mFakeReactionDetectionManager.onReactionDetected(EDGE_0_TO_2.getReaction(), true);
    mView.finishMedia();
    // Should navigate to edge 1a target index
    assertEquals(MEDIA_2, mView.getDisplayedMedia());
  }

  @Test public void nextMedia_reactionDetectedBeforeFinish() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, Arrays.asList(MEDIA_0, MEDIA_1),
        Collections.singletonList(EDGE_0_TO_1));
    initSceneViewModel();
    assertEquals(MEDIA_0, mView.getDisplayedMedia());
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a reaction
    mFakeReactionDetectionManager.onReactionDetected(EDGE_0_TO_1.getReaction(), true);
    // Should update next media
    assertEquals(MEDIA_1, mViewModel.getNextMedia());
    mView.finishMedia();
    // Should navigate to next media
    assertEquals(MEDIA_1, mView.getDisplayedMedia());
    // Should reset next media
    assertNull(mViewModel.getNextMedia());
    // Should stop reaction detection
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
  }

  @Test public void nextMedia_finishBeforeReactionDetected() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, Arrays.asList(MEDIA_0, MEDIA_1),
        Collections.singletonList(EDGE_0_TO_1));
    initSceneViewModel();
    assertEquals(MEDIA_0, mView.getDisplayedMedia());
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    mView.finishMedia();
    // Should not have next media yet
    assertNull(mViewModel.getNextMedia());
    // Detect a reaction
    mFakeReactionDetectionManager.onReactionDetected(EDGE_0_TO_1.getReaction(), true);
    // Should navigate to next media
    assertEquals(MEDIA_1, mView.getDisplayedMedia());
    // Should reset next media
    assertNull(mViewModel.getNextMedia());
    // Should stop reaction detection
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
  }

  @Test public void nextMedia_multipleLevels() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow,
        Arrays.asList(MEDIA_0, MEDIA_1, MEDIA_2), Arrays.asList(EDGE_0_TO_1, EDGE_1_TO_2));
    initSceneViewModel();
    mView.finishMedia();
    assertEquals(MEDIA_0, mView.getDisplayedMedia());
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a reaction
    mFakeReactionDetectionManager.onReactionDetected(EDGE_0_TO_1.getReaction(), true);
    // Should navigate to next media
    assertEquals(MEDIA_1, mView.getDisplayedMedia());
    mView.resetMediaFinished();
    mViewModel.onReady();
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a reaction and finish media
    mFakeReactionDetectionManager.onReactionDetected(EDGE_1_TO_2.getReaction(), true);
    mView.finishMedia();
    // Should navigate to next media
    assertEquals(MEDIA_2, mView.getDisplayedMedia());
  }

  @Test public void doView() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, MEDIA_0);
    initSceneViewModel();
    // Assert a view event is sent
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mMockWebServer.getRequestCount());
      }
    });
    InteractionEvent sentEvent = NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class);
    assertEquals(mScene.getId(), sentEvent.getSceneId());
    assertEquals(mFakeAuthManager.getCurrentUser().getId(), sentEvent.getUserId());
    assertEquals(0, sentEvent.getMediaId().longValue());
    assertEquals(EventType.VIEW, sentEvent.getEventType());
  }

  @Test public void reactionDetection() throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene = new Scene(SCENE_ID, DIRECTOR, reactionCounters, mNow, MEDIA_0);
    initSceneViewModel();
    // Wait for view event
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mMockWebServer.getRequestCount());
        InteractionEvent sentEvent =
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class);
        assertEquals(EventType.VIEW, sentEvent.getEventType());
      }
    });
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a reaction
    mFakeReactionDetectionManager.onReactionDetected(REACTION_2, true);
    // Wait for reaction event
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mMockWebServer.getRequestCount());
        InteractionEvent sentEvent =
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class);
        assertEquals(mScene.getId(), sentEvent.getSceneId());
        assertEquals(REACTION_2, sentEvent.getReaction());
        assertEquals(mFakeAuthManager.getCurrentUser().getId(), sentEvent.getUserId());
        assertEquals(0, sentEvent.getMediaId().longValue());
        assertEquals(EventType.REACTION, sentEvent.getEventType());
      }
    });
    // The detected reaction should be displayed
    assertEquals(REACTION_2.getDrawableResource(), mViewModel.mReactionDrawableResource.get());
    assertEquals("2", mViewModel.mReactionsCountText.get());
    assertEquals(SceneViewModel.POST_REACTION_COUNT_COLOR, mViewModel.mReactionCountColor.get());
    assertTrue(mView.wasReactionImageAnimated());
  }

  @Test public void reactionDetection_reactionIgnoredWithMultipleNextMediaOptions()
      throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene = new Scene(SCENE_ID, DIRECTOR, reactionCounters, mNow,
        Arrays.asList(MEDIA_0, MEDIA_1, MEDIA_2), Arrays.asList(EDGE_0_TO_1, EDGE_1_TO_2));
    initSceneViewModel();
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a non prevalent reaction
    mFakeReactionDetectionManager.onReactionDetected(EDGE_0_TO_2.getReaction(), false);
    // Wait for reaction to be detected.
    Thread.sleep(ViewModelTestSuite.DEFAULT_TIMEOUT.getValueInMS() / 2);
    // Should not detect a reaction
    assertEquals("1", mViewModel.mReactionsCountText.get());
  }

  @Test public void reactionDetection_reactionIgnoredWithoutNextMediaOptions() throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene = new Scene(SCENE_ID, DIRECTOR, reactionCounters, mNow, MEDIA_0);
    initSceneViewModel();
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a non prevalent reaction
    mFakeReactionDetectionManager.onReactionDetected(REACTION_2, false);
    // Wait for reaction to be detected.
    Thread.sleep(ViewModelTestSuite.DEFAULT_TIMEOUT.getValueInMS() / 2);
    // Should not detect a reaction
    assertEquals("1", mViewModel.mReactionsCountText.get());
  }

  @Test public void reactionDetection_reactionDetectedWithSingleNextMediaOption() throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene = new Scene(SCENE_ID, DIRECTOR, reactionCounters, mNow, Arrays.asList(MEDIA_0, MEDIA_1),
        Collections.singletonList(EDGE_0_TO_1));
    initSceneViewModel();
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Detect a non prevalent reaction
    mFakeReactionDetectionManager.onReactionDetected(EDGE_0_TO_1.getReaction(), false);
    // Should detect a reaction
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals("2", mViewModel.mReactionsCountText.get());
      }
    });
  }

  @Test public void reactionNotDetectedImmediately() throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene = new Scene(SCENE_ID, DIRECTOR, reactionCounters, mNow, MEDIA_0);
    initSceneViewModel();
    // Not detecting from the start.
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
  }

  @Test public void reactionNotDetectedWhenDirectorIsCurrentUser() throws Exception {
    TreeMap<Emotion, Long> reactionCounters = new TreeMap<Emotion, Long>() {{
      put(REACTION, 1L);
    }};
    mScene =
        new Scene(SCENE_ID, mFakeAuthManager.getCurrentUser(), reactionCounters, mNow, MEDIA_0);
    initSceneViewModel();
    mViewModel.onReady();
    // Wait for detection to start
    Thread.sleep(SceneViewModel.DETECTION_DELAY_MILLIS * 2);
    // Should not be subscribed to reaction detection.
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Fake a detection.
    mFakeReactionDetectionManager.onReactionDetected(REACTION, true);
    // Should not have an effect
    assertTrue(mViewModel.getCurrentDetectedReactions().isEmpty());
    assertEquals(reactionCounters, mViewModel.getScene().getReactionCounters());
  }

  @Test public void reactionNotDetectedOnHidden() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, REACTION_COUNTERS, mNow, MEDIA_0);
    initSceneViewModel();
    mViewModel.onReady();
    // Wait for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Hides view.
    mViewModel.onHidden();
    // Should not be subscribed to reaction detection.
    assertFalse(mFakeReactionDetectionManager.isSubscribed(mViewModel));
    // Do a detection
    mFakeReactionDetectionManager.onReactionDetected(REACTION, true);
    // Should not register the detection.
    assertTrue(mViewModel.getCurrentDetectedReactions().isEmpty());
  }

  @Test public void multipleDetections() throws Exception {
    mScene = new Scene(SCENE_ID, DIRECTOR, new TreeMap<Emotion, Long>(), mNow, MEDIA_0);
    initSceneViewModel();
    mViewModel.onReady();
    // Wait for view event and for detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        InteractionEvent sentEvent =
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class);
        assertEquals(EventType.VIEW, sentEvent.getEventType());
        assertTrue(mFakeReactionDetectionManager.isSubscribed(mViewModel));
      }
    });
    // Do a detection
    mFakeReactionDetectionManager.onReactionDetected(REACTION, true);
    // Display reaction emoji
    assertEquals(REACTION.getDrawableResource(), mViewModel.mReactionDrawableResource.get());
    // Should have been animated
    assertTrue(mView.wasReactionImageAnimated());
    mView.resetImageAnimated();
    // Should be reflected in scene reaction counters
    assertEquals(1, mViewModel.getScene().getReactionCounters().get(REACTION).longValue());
    // Repeat first reaction
    mFakeReactionDetectionManager.onReactionDetected(REACTION, true);
    // Should not change data model
    assertEquals(1, mViewModel.getScene().getReactionCounters().get(REACTION).longValue());
    // Should not be animated again
    assertFalse(mView.wasReactionImageAnimated());
    // Wait for reaction event to be sent
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        InteractionEvent sentEvent =
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class);
        assertEquals(REACTION, sentEvent.getReaction());
      }
    });

    // Do another detection
    mFakeReactionDetectionManager.onReactionDetected(REACTION_2, true);
    // Display reaction2 emoji
    assertEquals(REACTION_2.getDrawableResource(), mViewModel.mReactionDrawableResource.get());
    // Update reactions counter
    assertEquals("2", mViewModel.mReactionsCountText.get());
    // Should have been animated
    assertTrue(mView.wasReactionImageAnimated());
    mView.resetImageAnimated();
    // Should be reflected in scene reaction counters
    assertEquals(1, mViewModel.getScene().getReactionCounters().get(REACTION_2).longValue());
    // Wait for second reaction event to be sent
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        InteractionEvent sentEvent =
            NetworkUtil.GSON.fromJson(mLastRequest, InteractionEvent.class);
        assertEquals(REACTION_2, sentEvent.getReaction());
      }
    });

    int requestCount = mMockWebServer.getRequestCount();
    // Repeat first reaction
    mFakeReactionDetectionManager.onReactionDetected(REACTION, true);
    // Should not change data model
    assertEquals(1, mViewModel.getScene().getReactionCounters().get(REACTION).longValue());
    // Display reaction emoji
    assertEquals(REACTION.getDrawableResource(), mViewModel.mReactionDrawableResource.get());
    // Should have been animated
    assertTrue(mView.wasReactionImageAnimated());
    mView.resetImageAnimated();
    // Should not send a second reaction event
    Thread.sleep(100);
    assertEquals(requestCount, mMockWebServer.getRequestCount());
  }

  private void initSceneViewModel() throws Exception {
    mView = new ViewInterface();
    mViewModel = createViewModel(mViewModel.getClass(), (SceneViewInterface) mView, null);
    mViewModel.setScene(mScene);
    AppContainer.getReactionDetectionManager().start(null);
    mViewModel.onStart();
    mViewModel.onVisible();
    mViewModel.onReady();
  }

  private class ViewInterface extends UnitTestViewInterface implements SceneViewInterface {
    private boolean mImageAnimated = false;
    private Media mDisplayedMedia;
    private boolean mHasFinished;
    private Boolean reactionsLayoutExposed;

    @Override public void bounceReactionImage() {
      mImageAnimated = true;
    }

    @Override public void display(Media media) {
      mDisplayedMedia = media;
    }

    @Override public boolean hasMediaFinished() {
      return mHasFinished;
    }

    @Override public void fadeReactions() {
      reactionsLayoutExposed = false;
    }

    @Override public void exposeReactions() {
      reactionsLayoutExposed = true;
    }

    void resetMediaFinished() {
      mHasFinished = false;
    }

    void finishMedia() {
      mHasFinished = true;
      mViewModel.onFinished();
    }

    void resetImageAnimated() {
      mImageAnimated = false;
    }

    Media getDisplayedMedia() {
      return mDisplayedMedia;
    }

    boolean wasReactionImageAnimated() {
      return mImageAnimated;
    }
  }
}