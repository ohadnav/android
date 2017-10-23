package com.truethat.android.viewmodel;

import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
import com.truethat.android.model.Video;
import com.truethat.android.viewmodel.viewinterface.ScenesPagerViewInterface;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Test;
import retrofit2.Call;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */
@SuppressWarnings({ "RedundantCast", "serial" }) public class ScenesPagerViewModelTest
    extends ViewModelTestSuite {
  private static final long ID_1 = 1;
  private static final long ID_2 = 2;
  private static final String VIDEO_URL =
      "https://storage.googleapis.com/truethat-test-studio/testing/Ohad_wink_compressed.mp4";
  private static final String IMAGE_URL_1 =
      "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg";
  private static final String IMAGE_URL_2 =
      "http://s.hswstatic.com/gif/laughing-bonobo-360x240.jpg";
  private static final Date HOUR_AGO = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(1));
  private static final Date YESTERDAY = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(1));
  private static final long HAPPY_COUNT = 3000;
  private static final long FEAR_COUNT = HAPPY_COUNT + 1;
  private static final TreeMap<Emotion, Long> HAPPY_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, HAPPY_COUNT);
  }};
  private static final TreeMap<Emotion, Long> EMOTIONAL_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, HAPPY_COUNT);
    put(Emotion.OMG, FEAR_COUNT);
  }};
  private ScenesPagerViewModel mViewModel;
  private TheaterApi mApi;
  private List<Scene> mRespondedScenes;

  @Override public void setUp() throws Exception {
    super.setUp();
    mViewModel =
        createViewModel(ScenesPagerViewModel.class, (ScenesPagerViewInterface) new ViewInterface(),
            null);
    mViewModel.onStart();
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        String responseBody = NetworkUtil.GSON.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
    // Initialize api
    mApi = NetworkUtil.createApi(TheaterApi.class);
  }

  @Test public void displayScene() throws Exception {
    final Scene scene =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    mRespondedScenes = Collections.singletonList(scene);
    mViewModel.fetchScenes();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene, mViewModel.getDisplayedScene());
      }
    });
    assertFalse(mViewModel.mNonFoundLayoutVisibility.get());
    assertFalse(mViewModel.mLoadingImageVisibility.get());
    assertEquals(Collections.singletonList(scene), mViewModel.mItems);
  }

  @Test public void noScenesFound() throws Exception {
    mViewModel.fetchScenes();
    // Loading image should be shown
    assertTrue(mViewModel.mLoadingImageVisibility.get());
    // Non found should layout should be displayed instead of loading eventually.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mViewModel.mNonFoundLayoutVisibility.get());
      }
    });
    assertFalse(mViewModel.mLoadingImageVisibility.get());
    Scene scene = new Scene(ID_1, mFakeAuthManager.getCurrentUser(), new TreeMap<Emotion, Long>(),
        HOUR_AGO, new Photo(null, IMAGE_URL_1));
    // Explicitly load more scenes.
    mRespondedScenes = Collections.singletonList(scene);
    mViewModel.next();
    // Loading image should be shown
    assertTrue(mViewModel.mLoadingImageVisibility.get());
    // Not found text should be hidden.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(mViewModel.mNonFoundLayoutVisibility.get());
      }
    });
    // Scene should be displayed.
    assertEquals(scene, mViewModel.getDisplayedScene());
  }

  @Test public void noScenesFound_failedRequest() throws Exception {
    mMockWebServer.close();
    mViewModel.fetchScenes();
    // Not found text should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mViewModel.mNonFoundLayoutVisibility.get());
      }
    });
  }

  @Test public void noScenesFound_failedResponse() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    mViewModel.fetchScenes();
    // Not found text should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mViewModel.mNonFoundLayoutVisibility.get());
      }
    });
  }

  @Test public void nextScene() throws Exception {
    final Scene scene1 =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    Scene scene2 =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            new Photo(null, IMAGE_URL_2));
    mRespondedScenes = Arrays.asList(scene1, scene2);
    mViewModel.fetchScenes();
    // First scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene1, mViewModel.getDisplayedScene());
      }
    });
    // Triggers navigation to next scene.
    mViewModel.next();
    // Second scene should be displayed.
    assertEquals(scene2, mViewModel.getDisplayedScene());
  }

  @Test public void multipleTypes() throws Exception {
    final Scene photo =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    Scene video = new Scene(ID_2, mFakeAuthManager.getCurrentUser(), EMOTIONAL_REACTIONS, YESTERDAY,
        new Video(null, VIDEO_URL));
    mRespondedScenes = Arrays.asList(photo, video);
    mViewModel.fetchScenes();
    // First scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(photo, mViewModel.getDisplayedScene());
      }
    });
    // Triggers navigation to next scene.
    mViewModel.next();
    // Second scene should be displayed.
    assertEquals(video, mViewModel.getDisplayedScene());
  }

  @Test public void previousScene() throws Exception {
    final Scene scene1 =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    final Scene scene2 =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            new Photo(null, IMAGE_URL_2));
    mRespondedScenes = Arrays.asList(scene1, scene2);
    mViewModel.fetchScenes();
    // First scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene1, mViewModel.getDisplayedScene());
      }
    });
    // Triggers navigation to next scene.
    mViewModel.next();
    // Second scene should be displayed.
    assertEquals(scene2, mViewModel.getDisplayedScene());
    // Triggers navigation to previous scene.
    mViewModel.previous();
    // First scene should be displayed.
    assertEquals(scene1, mViewModel.getDisplayedScene());
    // Triggers navigation to previous scene.
    mViewModel.previous();
    // First scene should still be displayed.
    assertEquals(scene1, mViewModel.getDisplayedScene());
  }

  @Test public void nextSceneFetchesNewScenes() throws Exception {
    final Scene scene1 =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    final Scene scene2 =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            new Photo(null, IMAGE_URL_2));
    mRespondedScenes = Collections.singletonList(scene1);
    mViewModel.fetchScenes();
    // First scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene1, mViewModel.getDisplayedScene());
      }
    });
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(scene2);
    // Triggers navigation to next scene.
    mViewModel.next();
    // Second scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene2, mViewModel.getDisplayedScene());
      }
    });
  }

  @Test public void doNotAddDuplicateScenes_onFirstFetch() throws Exception {
    final Scene scene =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    mRespondedScenes = Arrays.asList(scene, scene);
    mViewModel.fetchScenes();
    // First scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene, mViewModel.getDisplayedScene());
      }
    });
    assertEquals(1, mViewModel.mItems.size());
  }

  @Test public void doNotAddDuplicateScenes() throws Exception {
    final Scene scene =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    mRespondedScenes = Collections.singletonList(scene);
    mViewModel.fetchScenes();
    // First scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene, mViewModel.getDisplayedScene());
      }
    });
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(scene);
    // Triggers navigation to next scene.
    mViewModel.next();
    Thread.sleep(DEFAULT_TIMEOUT.getValueInMS());
    assertEquals(1, mViewModel.mItems.size());
  }

  @Test public void judgeDuplicatesById() throws Exception {
    final Scene scene =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            new Photo(null, IMAGE_URL_1));
    mRespondedScenes = Collections.singletonList(scene);
    mViewModel.fetchScenes();
    // First scene should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene, mViewModel.getDisplayedScene());
      }
    });
    scene.getMediaNodes().remove(0);
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(scene);
    // Triggers navigation to next scene.
    mViewModel.next();
    Thread.sleep(DEFAULT_TIMEOUT.getValueInMS());
    assertEquals(1, mViewModel.mItems.size());
  }

  private class ViewInterface extends ViewModelTestSuite.UnitTestViewInterface
      implements ScenesPagerViewInterface {
    @Override public void displayItem(int index) {

    }

    @Override public Call<List<Scene>> buildFetchScenesCall() {
      return mApi.fetchScenes(mFakeAuthManager.getCurrentUser());
    }

    @Override public void vibrate() {

    }

    @Override public boolean isVisibleAndResumed() {
      return true;
    }
  }
}