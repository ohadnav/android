package com.truethat.android.viewmodel;

import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Pose;
import com.truethat.android.model.Reactable;
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
  private List<Pose> mRespondedPoses;

  @Override public void setUp() throws Exception {
    super.setUp();
    mViewModel = createViewModel(ReactablesPagerViewModel.class,
        (ReactablesPagerViewInterface) new ViewInterface());
    mViewModel.onStart();
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        String responseBody = NetworkUtil.GSON.toJson(mRespondedPoses);
        mRespondedPoses = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the poses list is empty.
    mRespondedPoses = Collections.emptyList();
    // Initialize api
    mApi = NetworkUtil.createApi(TheaterApi.class);
  }

  @Test public void displayReactable() throws Exception {
    final Pose pose =
        new Pose(ID_1, IMAGE_URL_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    mRespondedPoses = Collections.singletonList(pose);
    mViewModel.fetchReactables();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(pose, mViewModel.getDisplayedReactable());
      }
    });
    assertFalse(mViewModel.mLoadingLayoutVisibility.get());
    assertFalse(mViewModel.mNonFoundTextVisibility.get());
    assertEquals(Collections.singletonList(pose), mViewModel.mItems);
  }

  @Test public void noReactablesFound() throws Exception {
    mViewModel.fetchReactables();
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mViewModel.mLoadingLayoutVisibility.get());
        assertTrue(mViewModel.mNonFoundTextVisibility.get());
      }
    });
    Pose pose = new Pose(ID_1, IMAGE_URL_1, mFakeAuthManager.getCurrentUser(),
        new TreeMap<Emotion, Long>(),
            HOUR_AGO, Emotion.HAPPY);
    // Explicitly load more reactables.
    mRespondedPoses = Collections.singletonList(pose);
    mViewModel.next();
    // Not found text should be hidden.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertFalse(mViewModel.mLoadingLayoutVisibility.get());
        assertFalse(mViewModel.mNonFoundTextVisibility.get());
      }
    });
    // Wait until the reactable is displayed.
    assertEquals(pose, mViewModel.getDisplayedReactable());
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
    final Pose pose1 =
        new Pose(ID_1, IMAGE_URL_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    Pose pose2 = new Pose(ID_2, IMAGE_URL_2, mFakeAuthManager.getCurrentUser(), EMOTIONAL_REACTIONS,
            YESTERDAY,
            null);
    mRespondedPoses = Arrays.asList(pose1, pose2);
    mViewModel.fetchReactables();
    // First reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(pose1, mViewModel.getDisplayedReactable());
      }
    });
    // Triggers navigation to next reactable.
    mViewModel.next();
    // Second reactable should be displayed.
    assertEquals(pose2, mViewModel.getDisplayedReactable());
  }

  @Test public void previousReactable() throws Exception {
    final Pose pose1 =
        new Pose(ID_1, IMAGE_URL_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    final Pose pose2 =
        new Pose(ID_2, IMAGE_URL_2, mFakeAuthManager.getCurrentUser(), EMOTIONAL_REACTIONS,
            YESTERDAY,
            null);
    mRespondedPoses = Arrays.asList(pose1, pose2);
    mViewModel.fetchReactables();
    // First reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(pose1, mViewModel.getDisplayedReactable());
      }
    });
    // Triggers navigation to next reactable.
    mViewModel.next();
    // Second reactable should be displayed.
    assertEquals(pose2, mViewModel.getDisplayedReactable());
    // Triggers navigation to previous reactable.
    mViewModel.previous();
    // First reactable should be displayed.
    assertEquals(pose1, mViewModel.getDisplayedReactable());
    // Triggers navigation to previous reactable.
    mViewModel.previous();
    // First reactable should still be displayed.
    assertEquals(pose1, mViewModel.getDisplayedReactable());
  }

  @Test public void nextReactableFetchesNewReactables() throws Exception {
    final Pose pose1 =
        new Pose(ID_1, IMAGE_URL_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    final Pose pose2 =
        new Pose(ID_2, IMAGE_URL_2, mFakeAuthManager.getCurrentUser(), EMOTIONAL_REACTIONS,
            YESTERDAY,
            null);
    mRespondedPoses = Collections.singletonList(pose1);
    mViewModel.fetchReactables();
    // First reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(pose1, mViewModel.getDisplayedReactable());
      }
    });
    // Updates responded poses.
    mRespondedPoses = Collections.singletonList(pose2);
    // Triggers navigation to next reactable.
    mViewModel.next();
    // Second reactable should be displayed.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(pose2, mViewModel.getDisplayedReactable());
      }
    });
  }

  private class ViewInterface extends ViewModelTestSuite.UnitTestViewInterface
      implements ReactablesPagerViewInterface {
    @Override public void displayItem(int index) {

    }

    @Override public Call<List<Reactable>> buildFetchReactablesCall() {
      return mApi.fetchReactables(mFakeAuthManager.getCurrentUser());
    }

    @Override public boolean isReallyVisible() {
      return true;
    }
  }
}