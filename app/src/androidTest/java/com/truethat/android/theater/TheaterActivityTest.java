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
import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.Scene;
import com.truethat.android.common.camera.CameraTestUtil;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.empathy.DefaultReactionDetectionModule;
import com.truethat.android.empathy.Emotion;
import com.truethat.android.empathy.EmotionDetectionClassifier;
import com.truethat.android.empathy.ReactionDetectionModule;
import com.truethat.android.empathy.ReactionDetectionPubSub;
import com.truethat.android.identity.MockAuthModule;
import com.truethat.android.identity.User;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.truethat.android.BuildConfig.BACKEND_URL;
import static com.truethat.android.BuildConfig.PORT;
import static com.truethat.android.application.ApplicationTestUtil.isFullScreen;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */
public class TheaterActivityTest {
  private static final long ID_1 = 11;
  private static final long ID_2 = 22;
  // Dear lord fail this test when this image becomes unavailable ;)
  private static final String IMAGE_URL = "https://www.snapchat.com/global/social-lg.jpg";
  private static final Date HOUR_AGO = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(1));
  private static final Date YESTERDAY = new Date(new Date().getTime() - TimeUnit.DAYS.toMillis(1));
  private static final User DIRECTOR = new User(99, "James Cameron");
  private static final long HAPPY_COUNT = 3000;
  private static final long SAD_COUNT = 10000;
  private static final TreeMap<Emotion, Long> EMOTIONAL_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, HAPPY_COUNT);
    put(Emotion.SAD, SAD_COUNT);
  }};
  private static ReactionDetectionPubSub mDetectionPubSub;
  private static ReactionDetectionModule sMockDetectionModule = new ReactionDetectionModule() {
    @Override public void detect(ReactionDetectionPubSub detectionPubSub) {
      mDetectionPubSub = detectionPubSub;
    }

    @Override public void attempt(Image image) {
    }

    @Override public void stop() {
    }
  };
  private final MockWebServer mMockWebServer = new MockWebServer();
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  private List<Scene> mRespondedScenes;
  private int mPostEventCount;

  @BeforeClass public static void beforeClass() throws Exception {
    // Sets up the mocked permissions module.
    App.setPermissionsModule(new MockPermissionsModule(Permission.CAMERA));
    // Sets up the mocked auth module.
    App.setAuthModule(new MockAuthModule());
    // Sets up a mocked emotional reaction detection module.
    App.setReactionDetectionModule(sMockDetectionModule);
    // Sets the backend URL, for MockWebServer.
    NetworkUtil.setBackendUrl("http://localhost");
  }

  @AfterClass public static void afterClass() throws Exception {
    // Resets to default permissions module.
    App.setPermissionsModule(new DefaultPermissionsModule());
    // Restores default predefined backend url.
    NetworkUtil.setBackendUrl(BACKEND_URL);
  }

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    // Resets the post event counter.
    mPostEventCount = 0;
    // Starts mock server
    mMockWebServer.start(PORT);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (Objects.equals(request.getMethod(), "POST") && request.getPath().contains("theater")) {
          mPostEventCount++;
        }
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(NetworkUtil.GSON.toJson(mRespondedScenes) + "\n");
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
  }

  @After public void tearDown() throws Exception {
    // Closes mock server
    mMockWebServer.close();
  }

  @Test public void loadingScenes() throws Exception {
    mTheaterActivityTestRule.launchActivity(null);
    // Asserts progress bar is visible.
    onView(withId(R.id.progressBar)).check(matches(isDisplayed()));
    // Interim image is displayed.
    onView(withId(R.id.defaultImage)).check(matches(isDisplayed()));
  }

  @Test public void displayScene() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
    // Asserting the layout is displayed fullscreen.
    onView(withId(R.id.sceneLayout)).check(matches(isFullScreen()));
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
    // Asserting the displayed name is of the scene director
    onView(withId(R.id.directorNameText)).check(matches(withText(DIRECTOR.getName())));
    // Asserting the displayed time is represents the scene creation.
    onView(withId(R.id.sceneTimeAgoText)).check(
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
        new Scene(ID_1, IMAGE_URL, DIRECTOR, new TreeMap<Emotion, Long>(), HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
    // Asserting the reaction image is displayed and represents the default reaction.
    ImageView imageView =
        (ImageView) mTheaterActivityTestRule.getActivity().findViewById(R.id.reactionImage);
    assertTrue(CameraTestUtil.areDrawablesIdentical(imageView.getDrawable(),
        ContextCompat.getDrawable(mTheaterActivityTestRule.getActivity().getApplicationContext(),
            SceneLayout.DEFAULT_REACTION_COUNTER.getDrawableResource())));
  }

  @Test public void nextScene() throws Exception {
    mRespondedScenes =
        Arrays.asList(new Scene(ID_1, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null),
            new Scene(ID_2, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, YESTERDAY, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mPostEventCount);
      }
    });
    // Triggers navigation to next scene.
    onView(withId(R.id.theaterActivity)).perform(ViewActions.swipeDown());
    // Wait until the next scene is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(
        allOf(withId(R.id.sceneTimeAgoText), withText(DateUtil.formatTimeAgo(YESTERDAY))),
        TimeUnit.SECONDS.toMillis(3)));
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mPostEventCount);
      }
    });
  }

  @Test public void previousScene() throws Exception {
    mRespondedScenes =
        Arrays.asList(new Scene(ID_1, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null),
            new Scene(ID_2, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, YESTERDAY, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
    // Triggers navigation to next scene.
    onView(withId(R.id.theaterActivity)).perform(ViewActions.swipeDown());
    // Wait until the next scene is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.sceneTimeAgoText),
        withText(DateUtil.formatTimeAgo(YESTERDAY))), TimeUnit.SECONDS.toMillis(3)));
    // Triggers navigation to previous scene.
    onView(withId(R.id.theaterActivity)).perform(ViewActions.swipeUp());
    // Wait until the next scene is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(
        allOf(withId(R.id.sceneTimeAgoText), withText(DateUtil.formatTimeAgo(HOUR_AGO))),
        TimeUnit.SECONDS.toMillis(3)));
    // Triggers navigation to previous scene.
    onView(withId(R.id.theaterActivity)).perform(ViewActions.swipeUp());
    // Makes sure scene is unchanged
    onView(allOf(isDisplayed(), withId(R.id.sceneTimeAgoText),
        withText(DateUtil.formatTimeAgo(HOUR_AGO)))).check(matches(isDisplayed()));
  }

  @Test public void nextSceneFetchesNewScenes() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_2, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, YESTERDAY, null));
    // Triggers navigation to next scene.
    onView(withId(R.id.theaterActivity)).perform(ViewActions.swipeDown());
    // Wait until the next scene is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(
        allOf(withId(R.id.sceneTimeAgoText), withText(DateUtil.formatTimeAgo(YESTERDAY))),
        TimeUnit.SECONDS.toMillis(3)));
  }

  @Test public void emotionalReaction() throws Exception {
    Scene scene = new Scene(ID_1, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null);
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
    // Asserts that a view event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, mPostEventCount);
      }
    });
    Emotion lessCommon = EMOTIONAL_REACTIONS.firstKey();
    // ReactionDetectionPubSub reflects the first responded scene.
    mDetectionPubSub = mTheaterActivityTestRule.getActivity()
        .buildReactionDetectionPubSub(mRespondedScenes.get(0));
    mDetectionPubSub.onReactionDetected(lessCommon);
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
    Scene scene = new Scene(ID_1, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, lessCommon);
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
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
    // ReactionDetectionPubSub reflects the first responded scene.
    mDetectionPubSub = mTheaterActivityTestRule.getActivity()
        .buildReactionDetectionPubSub(mRespondedScenes.get(0));
  }

  @Test @MediumTest public void realEmotionalReaction() throws Exception {
    Scene scene = new Scene(ID_1, IMAGE_URL, DIRECTOR, EMOTIONAL_REACTIONS, HOUR_AGO, null);
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
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    // Wait until the scene is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.sceneLayout), TimeUnit.SECONDS.toMillis(3)));
    // Asserts that a reaction event was posted.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(2, mPostEventCount);
      }
    });
    App.setReactionDetectionModule(sMockDetectionModule);
  }
}