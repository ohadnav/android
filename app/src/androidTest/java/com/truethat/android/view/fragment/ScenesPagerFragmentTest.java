package com.truethat.android.view.fragment;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.truethat.android.R;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CameraTestUtil;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.model.Edge;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import com.truethat.android.model.Video;
import com.truethat.android.view.activity.TheaterActivity;
import com.truethat.android.viewmodel.SceneViewModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isFullscreen;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.common.network.NetworkUtil.GSON;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 26/06/2017 for TrueThat.
 */
public class ScenesPagerFragmentTest extends BaseApplicationTestSuite {
  private static final long ID_1 = 1;
  private static final long ID_2 = 2;
  private static final User DIRECTOR = new User(FakeAuthManager.USER_ID + 1, "Avi", "ci");
  private static final Video VIDEO = new Video(null,
      "https://storage.googleapis.com/truethat-test-studio/testing/Ohad_wink_compressed.mp4");
  private static final Photo PHOTO =
      new Photo(null, "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg");
  private static final Date HOUR_AGO = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(1));
  private static final long HAPPY_COUNT = 3000;
  @SuppressWarnings("serial") private static final TreeMap<Emotion, Long> HAPPY_REACTIONS =
      new TreeMap<Emotion, Long>() {{
        put(Emotion.HAPPY, HAPPY_COUNT);
      }};
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  private List<Scene> mRespondedScenes;

  @SuppressWarnings("ConstantConditions")
  public static void assertSceneDisplayed(final Scene scene, User currentUser,
      final int mediaIndex) {
    final ScenesPagerFragment pagerFragment =
        (ScenesPagerFragment) getCurrentActivity().getSupportFragmentManager()
            .findFragmentById(R.id.scenesPagerFragment);
    // Wait until the scene is displayed.
    waitMatcher(withId(R.id.sceneFragment));
    // Wait until the correct fragment is the current one.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene.getId(), pagerFragment.getDisplayedScene().getScene().getId());
      }
    });
    final SceneFragment currentFragment = pagerFragment.getDisplayedScene();
    // Wait for the right media to display
    await().atMost(Duration.FIVE_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(mediaIndex,
            scene.getMediaNodes().indexOf(currentFragment.getViewModel().getCurrentMedia()));
      }
    });
    // Wait until the fragment is ready
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(currentFragment.getMediaFragment().isReady());
      }
    });
    if (scene.getMediaNodes().get(mediaIndex) instanceof Photo) {
      // Asserting the image is displayed fullscreen.
      waitMatcher(allOf(withId(R.id.imageView), isDisplayed()));
      assertTrue(isFullscreen(currentFragment.getView().findViewById(R.id.imageView)));
    } else if (scene.getMediaNodes().get(mediaIndex) instanceof Video) {
      // Asserting the video is displayed fullscreen.
      assertTrue(isFullscreen(currentFragment.getView().findViewById(R.id.videoSurface)));
      // Video should be playing
      await().untilAsserted(new ThrowingRunnable() {
        @Override public void run() throws Throwable {
          assertTrue(((VideoFragment) pagerFragment.getDisplayedScene()
              .getMediaFragment()).getMediaPlayer().isPlaying());
        }
      });
    }
    // Loading layout should be hidden.
    onView(withId(R.id.nonFoundLayout)).check(matches(not(isDisplayed())));
    ImageView reactionImage = currentFragment.getView().findViewById(R.id.reactionImage);
    // If there are no reactions they shouldn't be displayed
    if (NumberUtil.sum(scene.getReactionCounters()) > 0) {
      // Asserting the reactions count is abbreviated.
      assertEquals(NumberUtil.format(NumberUtil.sum(scene.getReactionCounters())),
          ((TextView) currentFragment.getView().findViewById(R.id.reactionsCountText)).getText());
    } else {
      assertEquals("",
          ((TextView) currentFragment.getView().findViewById(R.id.reactionsCountText)).getText());
      assertTrue(CameraTestUtil.areDrawablesIdentical(reactionImage.getDrawable(),
          ContextCompat.getDrawable(pagerFragment.getActivity().getApplicationContext(),
              R.drawable.transparent_1x1)));
    }
    // Asserting the displayed time is represents the scene creation.
    assertEquals(DateUtil.formatTimeAgo(scene.getCreated()),
        ((TextView) currentFragment.getView().findViewById(R.id.timeAgoText)).getText());
    // Should not display director name if the current user is the director.
    if (!Objects.equals(scene.getDirector().getId(), currentUser.getId())) {
      assertEquals(View.VISIBLE,
          currentFragment.getView().findViewById(R.id.directorNameText).getVisibility());
      // Asserting the displayed name is of the scene director
      assertEquals(scene.getDirector().getDisplayName(),
          ((TextView) currentFragment.getView().findViewById(R.id.directorNameText)).getText());
    } else {
      assertEquals(View.GONE,
          currentFragment.getView().findViewById(R.id.directorNameText).getVisibility());
    }
  }

  @BeforeClass public static void beforeClass() throws Exception {
    SceneViewModel.setDetectionDelayMillis(100);
  }

  @Before public void setUp() throws Exception {
    super.setUp();
    // Resets the post event counter.
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = GSON.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the list is empty.
    mRespondedScenes = Collections.emptyList();
  }

  @Test public void displayPhoto() throws Exception {
    Scene scene =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, PHOTO);
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    assertSceneDisplayed(scene, mFakeAuthManager.getCurrentUser(), 0);
  }

  @Test public void displayVideo() throws Exception {
    Scene video =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, VIDEO);
    mRespondedScenes = Collections.singletonList(video);
    mTheaterActivityTestRule.launchActivity(null);
    assertSceneDisplayed(video, mFakeAuthManager.getCurrentUser(), 0);
  }

  @Test public void displayMultipleTypes() throws Exception {
    Scene photo =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, PHOTO);
    Scene video =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, VIDEO);
    mRespondedScenes = Arrays.asList(photo, video);
    mTheaterActivityTestRule.launchActivity(null);
    assertSceneDisplayed(photo, mFakeAuthManager.getCurrentUser(), 0);
    // Swipe to next scene
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    assertSceneDisplayed(video, mFakeAuthManager.getCurrentUser(), 0);
  }

  @Test public void reactionDetection() throws Exception {
    Scene scene = new Scene(ID_1, DIRECTOR, HAPPY_REACTIONS, HOUR_AGO, PHOTO);
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    assertSceneDisplayed(scene, mFakeAuthManager.getCurrentUser(), 0);
    final ScenesPagerFragment scenesPagerFragment =
        (ScenesPagerFragment) getCurrentActivity().getSupportFragmentManager()
            .findFragmentById(R.id.scenesPagerFragment);
    // Wait for reaction detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            scenesPagerFragment.getDisplayedScene().getViewModel()));
      }
    });
    mFakeReactionDetectionManager.doDetection(Emotion.SURPRISE);
    @SuppressWarnings("ConstantConditions") final ImageView reactionImage =
        scenesPagerFragment.getDisplayedScene().getView().findViewById(R.id.reactionImage);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(CameraTestUtil.areDrawablesIdentical(
            ContextCompat.getDrawable(mActivityTestRule.getActivity(),
                Emotion.SURPRISE.getDrawableResource()), reactionImage.getDrawable()));
      }
    });
  }

  @Test public void evolvingScene() throws Exception {
    Scene scene = new Scene(ID_1, DIRECTOR, HAPPY_REACTIONS, HOUR_AGO, Arrays.asList(VIDEO, PHOTO),
        Collections.singletonList(new Edge(0, 1, Emotion.SURPRISE)));
    mRespondedScenes = Collections.singletonList(scene);
    mTheaterActivityTestRule.launchActivity(null);
    assertSceneDisplayed(scene, mFakeAuthManager.getCurrentUser(), 0);
    final ScenesPagerFragment scenesPagerFragment =
        (ScenesPagerFragment) getCurrentActivity().getSupportFragmentManager()
            .findFragmentById(R.id.scenesPagerFragment);
    // Wait for reaction detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            scenesPagerFragment.getDisplayedScene().getViewModel()));
      }
    });
    mFakeReactionDetectionManager.doDetection(Emotion.SURPRISE);
    assertSceneDisplayed(scene, mFakeAuthManager.getCurrentUser(), 1);
  }
}