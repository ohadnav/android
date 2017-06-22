package com.truethat.android.theater;

import android.media.Image;
import android.support.annotation.Nullable;
import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.auth.User;
import com.truethat.android.common.BaseApplicationTest;
import com.truethat.android.common.camera.CameraTestUtil;
import com.truethat.android.common.media.ReactableFragment;
import com.truethat.android.common.media.Scene;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.empathy.DefaultReactionDetectionModule;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.empathy.EmotionDetectionClassifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.truethat.android.application.ApplicationTestUtil.isFullScreen;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */
public class TheaterActivityTest extends BaseApplicationTest {
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
  private static final TreeMap<Emotion, Long> EMOTIONAL_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, HAPPY_COUNT);
    put(Emotion.SAD, SAD_COUNT);
  }};
  private final User DIRECTOR = new User(99L, "James", "Cameron", "avatar", "+1000000000");
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  private List<Scene> mRespondedScenes;
  private int mPostEventCount;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Resets the post event counter.
    mPostEventCount = 0;
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (Objects.equals(request.getMethod(), "POST") && request.getPath().contains("theater")) {
          mPostEventCount++;
        }
        String responseBody = NetworkUtil.GSON.toJson(mRespondedScenes) + "\n";
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
  }

  @Test public void loadingScenes() throws Exception {
    mTheaterActivityTestRule.launchActivity(null);
    // Interim image is displayed.
    onView(withId(R.id.loadingImage)).check(matches(isDisplayed()));
  }

  @Test public void displayScene() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
    // Asserting the layout is displayed fullscreen.
    onView(withId(R.id.reactableFragment)).check(matches(isFullScreen()));
    // Loading image should be hidden.
    onView(withId(R.id.loadingImage)).check(matches(not(isDisplayed())));
    // Asserting the reactions count is abbreviated.
    onView(withId(R.id.reactionCountText)).check(
        matches(withText(NumberUtil.format(HAPPY_COUNT + SAD_COUNT))));
    // Asserting the reaction image is displayed and represents the most common reaction.
    onView(withId(R.id.reactionImage)).check(matches(isDisplayed()));
    ImageView imageView =
        (ImageView) mTheaterActivityTestRule.getActivity().findViewById(R.id.reactionImage);
    assertTrue(CameraTestUtil.areDrawablesIdentical(imageView.getDrawable(),
        ContextCompat.getDrawable(mTheaterActivityTestRule.getActivity().getApplicationContext(),
            EMOTIONAL_REACTIONS.lastKey().getDrawableResource())));
    // Asserting the scene image is displayed fullscreen.
    onView(withId(R.id.sceneImage)).check(matches(isFullScreen()));
    // Asserting the displayed name is of the reactable director
    onView(withId(R.id.directorNameText)).check(matches(withText(DIRECTOR.getDisplayName())));
    // Asserting the displayed time is represents the reactable creation.
    onView(withId(R.id.timeAgoText)).check(
        matches(withText(DateUtil.formatTimeAgo(HOUR_AGO))));
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mPostEventCount);
      }
    });
  }

  @Test public void displayScene_noReaction() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, DIRECTOR, new TreeMap<Emotion, Long>(), HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
    // Asserting the reaction image is displayed and represents the default reaction.
    ImageView imageView =
        (ImageView) mTheaterActivityTestRule.getActivity().findViewById(R.id.reactionImage);
    assertTrue(CameraTestUtil.areDrawablesIdentical(imageView.getDrawable(),
        ContextCompat.getDrawable(mTheaterActivityTestRule.getActivity().getApplicationContext(),
            ReactableFragment.DEFAULT_REACTION_COUNTER.getDrawableResource())));
  }

  // Test is unstable, run individually if needed.
  @Test public void nextScene() throws Exception {
    mRespondedScenes =
        Arrays.asList(new Scene(ID_1, IMAGE_URL_1, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null),
            new Scene(ID_2, IMAGE_URL_2, DIRECTOR, EMOTIONAL_REACTIONS, YESTERDAY, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
    // Asserts that a single view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mPostEventCount);
      }
    });
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Wait until the next reactable is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(
        allOf(withId(R.id.timeAgoText), withText(DateUtil.formatTimeAgo(YESTERDAY))),
        TimeUnit.SECONDS.toMillis(1)));
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mPostEventCount);
      }
    });
  }

  @Test public void previousScene() throws Exception {
    mRespondedScenes =
        Arrays.asList(new Scene(ID_1, IMAGE_URL_1, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null),
            new Scene(ID_2, IMAGE_URL_2, DIRECTOR, EMOTIONAL_REACTIONS, YESTERDAY, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Wait until the next reactable is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(
        allOf(isDisplayed(), withId(R.id.timeAgoText), withText(DateUtil.formatTimeAgo(YESTERDAY))),
        TimeUnit.SECONDS.toMillis(1)));
    // Triggers navigation to previous reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeRight());
    // Wait until the next reactable is displayed, or throw otherwise.
    onView(isRoot()).perform(
        waitMatcher(allOf(withId(R.id.timeAgoText), withText(DateUtil.formatTimeAgo(HOUR_AGO))),
            TimeUnit.SECONDS.toMillis(1)));
    // Triggers navigation to previous reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeRight());
    // Makes sure reactable is unchanged
    onView(allOf(isDisplayed(), withId(R.id.timeAgoText),
        withText(DateUtil.formatTimeAgo(HOUR_AGO)))).check(matches(isDisplayed()));
  }

  @Test public void nextSceneFetchesNewScenes() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_2, IMAGE_URL_2, DIRECTOR, EMOTIONAL_REACTIONS, YESTERDAY, null));
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Wait until the next reactable is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(
        allOf(withId(R.id.timeAgoText), withText(DateUtil.formatTimeAgo(YESTERDAY))),
        TimeUnit.SECONDS.toMillis(1)));
  }

  @Test public void emotionalReaction() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mPostEventCount);
      }
    });
    Emotion lessCommon = EMOTIONAL_REACTIONS.firstKey();
    // Do the detection
    mMockReactionDetectionModule.doDetection(lessCommon);
    // Asserts that a reaction event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mPostEventCount);
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
        new Scene(ID_1, IMAGE_URL_1, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, lessCommon));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
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
        assertEquals(1, mPostEventCount);
      }
    });
    // Already detected reaction, and so reaction detection should be stopped.
    assertFalse(mMockReactionDetectionModule.isDetecting());
  }

  @Test @MediumTest public void cameraInputRequestsForDetection() throws Exception {
    App.setReactionDetectionModule(
        new DefaultReactionDetectionModule(new EmotionDetectionClassifier() {
          boolean isFirst = true;

          @Nullable @Override public Emotion classify(Image image) {
            Emotion reaction = Emotion.HAPPY;
            if (isFirst) {
              isFirst = false;
              reaction = null;
            }
            return reaction;
          }
        }));
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(
        waitMatcher(withId(R.id.reactableFragment), TimeUnit.SECONDS.toMillis(1)));
    // Asserts that a reaction event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mPostEventCount);
      }
    });
  }
}