package com.truethat.android.view.activity;

import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.InteractionApi;
import com.truethat.android.common.util.CameraTestUtil;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import com.truethat.android.view.fragment.CameraFragment;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.centerSwipeUp;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */
public class TheaterActivityTest extends BaseApplicationTestSuite {
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
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  private User mDirector;
  private List<Scene> mRespondedScenes;

  @Before public void setUp() throws Exception {
    super.setUp();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = mGson.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
    // Initialize director
    mDirector = new User(99L, "James", "Cameron", mFakeDeviceManager.getDeviceId(),
        mFakeDeviceManager.getPhoneNumber());
  }

  @Test public void navigation() throws Exception {
    mTheaterActivityTestRule.launchActivity(null);
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
  }

  @Test public void navigationWhileReactableDisplayed() throws Exception {
    Scene scene = new Scene(ID_1, IMAGE_URL_1, mDirector, EMOTIONAL_REACTIONS, HOUR_AGO, null);
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    ReactablesPagerActivityTest.assertReactableDisplayed(scene, mFakeAuthManager.currentUser());
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
  }

  @Test @FlakyTest public void singleInstance() throws Exception {
    Scene scene = new Scene(1L, "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg",
        mDirector, new TreeMap<Emotion, Long>(), new Date(), null);
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    ReactablesPagerActivityTest.assertReactableDisplayed(scene, mFakeAuthManager.currentUser());
    // Navigate out of Theater activity
    centerSwipeUp();
    waitForActivity(StudioActivity.class);
    // Navigate back to Theater activity
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(TheaterActivity.class);
    ReactablesPagerActivityTest.assertReactableDisplayed(scene, mFakeAuthManager.currentUser());
  }

  @Test public void cameraPreviewIsHidden() throws Exception {
    Scene scene = new Scene(ID_1, IMAGE_URL_1, mDirector, EMOTIONAL_REACTIONS, HOUR_AGO, null);
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    // Camera preview should be hidden
    final CameraFragment cameraFragment = (CameraFragment) mTheaterActivityTestRule.getActivity()
        .getSupportFragmentManager()
        .findFragmentById(R.id.cameraFragment);
    // Wait until camera is open, so that preview can be potentially shown.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return cameraFragment.isCameraOpen();
      }
    });
    assertEquals(View.GONE, cameraFragment.getCameraPreview().getVisibility());
  }

  @Test public void viewEvents() throws Exception {
    Scene scene1 = new Scene(ID_1, IMAGE_URL_1, mDirector, HAPPY_REACTIONS, HOUR_AGO, null);
    Scene scene2 = new Scene(ID_2, IMAGE_URL_2, mDirector, EMOTIONAL_REACTIONS, YESTERDAY, null);
    mRespondedScenes = Arrays.asList(scene1, scene2);
    mTheaterActivityTestRule.launchActivity(null);
    // First scene should be displayed.
    ReactablesPagerActivityTest.assertReactableDisplayed(scene1, mFakeAuthManager.currentUser());
    // Asserts that a single view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mDispatcher.getCount(InteractionApi.PATH));
      }
    });
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    ReactablesPagerActivityTest.assertReactableDisplayed(scene2, mFakeAuthManager.currentUser());
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mDispatcher.getCount(InteractionApi.PATH));
      }
    });
  }

  @Test public void emotionalReaction() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, mDirector, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mDispatcher.getCount(InteractionApi.PATH));
      }
    });
    // Assert the detection is ongoing
    assertTrue(mFakeReactionDetectionManager.isDetecting());
    Emotion lessCommon = EMOTIONAL_REACTIONS.firstKey();
    // Do the detection
    mFakeReactionDetectionManager.doDetection(lessCommon);
    // Asserts that a reaction event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mDispatcher.getCount(InteractionApi.PATH));
      }
    });
    // Asserting the reaction image is changed to reflect the user reaction.
    ImageView imageView =
        (ImageView) mTheaterActivityTestRule.getActivity().findViewById(R.id.reactionImage);
    assertTrue(CameraTestUtil.areDrawablesIdentical(imageView.getDrawable(),
        ContextCompat.getDrawable(mTheaterActivityTestRule.getActivity().getApplicationContext(),
            lessCommon.getDrawableResource())));
  }

  @Test public void displayScene_alreadyReacted() throws Exception {
    Emotion lessCommon = EMOTIONAL_REACTIONS.firstKey();
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, mDirector, EMOTIONAL_REACTIONS, HOUR_AGO, lessCommon));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    waitMatcher(withId(R.id.reactableFragment));
    // Asserting the reaction image is displayed and represents the user reaction.
    onView(withId(R.id.reactionImage)).check(matches(isDisplayed()));
    ImageView imageView =
        (ImageView) mTheaterActivityTestRule.getActivity().findViewById(R.id.reactionImage);
    assertTrue(CameraTestUtil.areDrawablesIdentical(imageView.getDrawable(),
        ContextCompat.getDrawable(mTheaterActivityTestRule.getActivity().getApplicationContext(),
            lessCommon.getDrawableResource())));
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mDispatcher.getCount(InteractionApi.PATH));
      }
    });
    // Already detected reaction, and so reaction detection should be stopped.
    assertFalse(mFakeReactionDetectionManager.isDetecting());
  }
}