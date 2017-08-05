package com.truethat.android.viewmodel;

import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.viewmodel.viewinterface.ReactablesPagerViewInterface;
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
public class ReactablesPagerViewModelTest extends ViewModelTestSuite {
  private static final long ID_1 = 1;
  private static final long ID_2 = 2;
  private static final String IMAGE_URL_1 =
      "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg";
  private static final String IMAGE_URL_2 =
      "http://s.hswstatic.com/gif/laughing-bonobo-360x240.jpg";
  private static final Date HOUR_AGO = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(1));
  private static final Date YESTERDAY = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(1));
  private static final long HAPPY_COUNT = 3000;
  private static final long SAD_COUNT = HAPPY_COUNT + 1;
  private static final TreeMap<Emotion, Long> HAPPY_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, HAPPY_COUNT);
  }};
  private static final TreeMap<Emotion, Long> EMOTIONAL_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, HAPPY_COUNT);
    put(Emotion.SAD, SAD_COUNT);
  }};
  private ReactablesPagerViewModel mViewModel;
  private TheaterApi mApi;
  private List<Scene> mRespondedScenes;

  @Override public void setUp() throws Exception {
    super.setUp();
    mViewModel = createViewModel(ReactablesPagerViewModel.class,
        (ReactablesPagerViewInterface) new ViewInterface());
    mViewModel.onStart();
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        String responseBody = mGson.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
    // Initialize api
    mApi = mViewModel.createApiInterface(TheaterApi.class);
  }

  @Test public void displayReactable() throws Exception {
    final Scene scene =
        new Scene(ID_1, IMAGE_URL_1, mFakeAuthManager.currentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    mRespondedScenes = Collections.singletonList(scene);
    mViewModel.fetchReactables();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene, mViewModel.getDisplayedReactable());
      }
    });
    assertFalse(mViewModel.mLoadingLayoutVisibility.get());
    assertFalse(mViewModel.mNonFoundTextVisibility.get());
    assertEquals(Collections.singletonList(scene), mViewModel.mItems);
  }

  @Test public void noReactablesFound() throws Exception {
    mViewModel.fetchReactables();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mViewModel.mLoadingLayoutVisibility.get());
        assertTrue(mViewModel.mNonFoundTextVisibility.get());
      }
    });
    Scene scene =
        new Scene(ID_1, IMAGE_URL_1, mFakeAuthManager.currentUser(), new TreeMap<Emotion, Long>(),
            HOUR_AGO, Emotion.HAPPY);
    // Explicitly load more reactables.
    mRespondedScenes = Collections.singletonList(scene);
    mViewModel.next();
    // Not found text should be hidden.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(mViewModel.mLoadingLayoutVisibility.get());
        assertFalse(mViewModel.mNonFoundTextVisibility.get());
      }
    });
    // Wait until the reactable is displayed.
    assertEquals(scene, mViewModel.getDisplayedReactable());
  }

  @Test public void noReactablesFound_failedRequest() throws Exception {
    mMockWebServer.close();
    mViewModel.fetchReactables();
    // Not found text should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mViewModel.mNonFoundTextVisibility.get());
      }
    });
  }

  @Test public void noReactablesFound_failedResponse() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    mViewModel.fetchReactables();
    // Not found text should be displayed.
    // Not found text should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mViewModel.mLoadingLayoutVisibility.get());
        assertTrue(mViewModel.mNonFoundTextVisibility.get());
      }
    });
  }

  @Test public void nextReactable() throws Exception {
    final Scene scene1 =
        new Scene(ID_1, IMAGE_URL_1, mFakeAuthManager.currentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    Scene scene2 =
        new Scene(ID_2, IMAGE_URL_2, mFakeAuthManager.currentUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            null);
    mRespondedScenes = Arrays.asList(scene1, scene2);
    mViewModel.fetchReactables();
    // First reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene1, mViewModel.getDisplayedReactable());
      }
    });
    // Triggers navigation to next reactable.
    mViewModel.next();
    // Second reactable should be displayed.
    assertEquals(scene2, mViewModel.getDisplayedReactable());
  }

  @Test public void previousReactable() throws Exception {
    final Scene scene1 =
        new Scene(ID_1, IMAGE_URL_1, mFakeAuthManager.currentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    final Scene scene2 =
        new Scene(ID_2, IMAGE_URL_2, mFakeAuthManager.currentUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            null);
    mRespondedScenes = Arrays.asList(scene1, scene2);
    mViewModel.fetchReactables();
    // First reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene1, mViewModel.getDisplayedReactable());
      }
    });
    // Triggers navigation to next reactable.
    mViewModel.next();
    // Second reactable should be displayed.
    assertEquals(scene2, mViewModel.getDisplayedReactable());
    // Triggers navigation to previous reactable.
    mViewModel.previous();
    // First reactable should be displayed.
    assertEquals(scene1, mViewModel.getDisplayedReactable());
    // Triggers navigation to previous reactable.
    mViewModel.previous();
    // First reactable should still be displayed.
    assertEquals(scene1, mViewModel.getDisplayedReactable());
  }

  @Test public void nextReactableFetchesNewReactables() throws Exception {
    final Scene scene1 =
        new Scene(ID_1, IMAGE_URL_1, mFakeAuthManager.currentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    final Scene scene2 =
        new Scene(ID_2, IMAGE_URL_2, mFakeAuthManager.currentUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            null);
    mRespondedScenes = Collections.singletonList(scene1);
    mViewModel.fetchReactables();
    // First reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene1, mViewModel.getDisplayedReactable());
      }
    });
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(scene2);
    // Triggers navigation to next reactable.
    mViewModel.next();
    // Second reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene2, mViewModel.getDisplayedReactable());
      }
    });
  }

  private class ViewInterface extends ViewModelTestSuite.UnitTestViewInterface
      implements ReactablesPagerViewInterface {
    @Override public void displayItem(int index) {

    }

    @Override public Call<List<Reactable>> buildFetchReactablesCall() {
      return mApi.fetchReactables(mFakeAuthManager.currentUser());
    }

    @Override public boolean isReallyVisible() {
      return true;
    }
  }
}