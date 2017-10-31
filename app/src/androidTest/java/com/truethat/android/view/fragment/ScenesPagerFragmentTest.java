package com.truethat.android.view.fragment;

import android.support.constraint.ConstraintLayout;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.auth.FakeAuthManager;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.common.util.CameraTestUtil;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.model.Edge;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Media;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import com.truethat.android.model.Video;
import com.truethat.android.view.activity.MainActivity;
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
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 26/06/2017 for TrueThat.
 */
@SuppressWarnings("ConstantConditions") public class ScenesPagerFragmentTest
    extends BaseInstrumentationTestSuite {
  private static final long ID_1 = 1;
  private static final long ID_2 = 2;
  private static final User DIRECTOR = new User(FakeAuthManager.USER_ID + 1, "Avi", "ci");
  private static final Video VIDEO = new Video(0L,
      "https://storage.googleapis.com/truethat-test-studio/testing/Ohad_wink_compressed.mp4");
  private static final Photo PHOTO = new Photo(1L,
      "https://drive.google.com/file/d/0B4xnu-ma8esCQVM2NC0tS0JxZkU/view?usp=sharing");
  private static final Date HOUR_AGO = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(1));
  private static final long HAPPY_COUNT = 3000;
  @SuppressWarnings("serial") private static final TreeMap<Emotion, Long> HAPPY_REACTIONS =
      new TreeMap<Emotion, Long>() {{
        put(Emotion.HAPPY, 1L);
      }};
  private List<Scene> mRespondedScenes;

  @SuppressWarnings("ConstantConditions")
  public static void assertSceneDisplayed(final Scene scene, final long mediaId) {
    if (!(getCurrentActivity() instanceof MainActivity)) {
      throw new IllegalStateException(
          "Scenes can only be displayed in main activity, current activity is "
              + getCurrentActivity().getClass().getSimpleName());
    }
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertNotNull(getScenesPagerFragment());
      }
    });
    final ScenesPagerFragment pagerFragment = getScenesPagerFragment();
    // Wait until scene is displayed
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertNotNull(pagerFragment.getCurrentFragment());
      }
    });
    // Wait until the correct scene fragment is the current one.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(scene.getId(), pagerFragment.getCurrentFragment().getScene().getId());
      }
    });
    final SceneFragment currentFragment = pagerFragment.getCurrentFragment();
    // Wait for the right media to display
    await().atMost(Duration.FIVE_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(mediaId, currentFragment.getViewModel().getCurrentMedia().getId().longValue());
      }
    });
    // Wait until the fragment is ready
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(currentFragment.getMediaFragment().isReady());
      }
    });
    final Media displayedMedia = currentFragment.getViewModel().getCurrentMedia();
    if (displayedMedia instanceof Photo) {
      // Asserting the image is displayed fullscreen.
      waitMatcher(allOf(withId(R.id.imageView), isDisplayed()));
      assertTrue(isFullscreen(currentFragment.getView().findViewById(R.id.imageView)));
    } else if (displayedMedia instanceof Video) {
      // Asserting the video is displayed fullscreen.
      assertTrue(isFullscreen(currentFragment.getView().findViewById(R.id.videoTextureView)));
      // Video should be playing
      await().untilAsserted(new ThrowingRunnable() {
        @Override public void run() throws Throwable {
          assertTrue(((VideoFragment) pagerFragment.getCurrentFragment()
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
    if (!Objects.equals(scene.getDirector().getId(),
        AppContainer.getAuthManager().getCurrentUser().getId())) {
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

  private static ScenesPagerFragment getScenesPagerFragment() {
    MainActivity mainActivity = (MainActivity) getCurrentActivity();
    if (mainActivity.getMainPager().getCurrentItem() == MainActivity.TOOLBAR_STUDIO_INDEX) {
      throw new IllegalStateException("Scenes cannot be displayed in the studio.");
    }
    final MainFragment mainFragment = mainActivity.getCurrentMainFragment();
    return (ScenesPagerFragment) mainFragment.getFragmentManager()
        .findFragmentById(mainFragment instanceof TheaterFragment ? R.id.theater_scenesPagerLayout
            : R.id.repertoire_scenesPagerLayout);
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
    MainActivity.sLaunchIndex = MainActivity.TOOLBAR_THEATER_INDEX;
  }

  @Override public void tearDown() throws Exception {
    super.tearDown();
    MainActivity.sLaunchIndex = MainActivity.TOOLBAR_STUDIO_INDEX;
  }

  @Test public void displayPhoto() throws Exception {
    Scene scene =
        new Scene(ID_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, PHOTO);
    mRespondedScenes = Collections.singletonList(scene);
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    mFakeReactionDetectionManager.onFaceDetectionStarted();
    assertSceneDisplayed(scene, PHOTO.getId());
  }

  @Test public void displayVideo() throws Exception {
    Scene video =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, VIDEO);
    mRespondedScenes = Collections.singletonList(video);
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    assertSceneDisplayed(video, VIDEO.getId());
  }

  @Test public void scenesNavigation() throws Exception {
    Scene evolving =
        new Scene(ID_1, DIRECTOR, HAPPY_REACTIONS, HOUR_AGO, Arrays.asList(VIDEO, PHOTO),
            Collections.singletonList(new Edge(VIDEO.getId(), PHOTO.getId(), Emotion.OMG)));
    Scene video =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, VIDEO);
    mRespondedScenes = Arrays.asList(evolving, video);
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    assertSceneDisplayed(evolving, VIDEO.getId());
    final ScenesPagerFragment scenesPagerFragment = getScenesPagerFragment();
    // Wait for reaction detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            scenesPagerFragment.getCurrentFragment().getViewModel()));
      }
    });
    mFakeReactionDetectionManager.onReactionDetected(Emotion.OMG, true);
    // Should display the follow up media
    assertSceneDisplayed(evolving, PHOTO.getId());
    // Tap for next scene
    onView(withId(android.R.id.content)).perform(
        new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER_RIGHT, Press.FINGER));
    assertSceneDisplayed(video, VIDEO.getId());
    // Tap for previous scene
    onView(withId(android.R.id.content)).perform(
        new GeneralClickAction(Tap.SINGLE, GeneralLocation.CENTER_LEFT, Press.FINGER));
    // Should save the previous scene state.
    assertSceneDisplayed(evolving, PHOTO.getId());
  }

  @Test public void scenesStateSaved() throws Exception {
    Scene evolving =
        new Scene(ID_1, DIRECTOR, HAPPY_REACTIONS, HOUR_AGO, Arrays.asList(VIDEO, PHOTO),
            Collections.singletonList(new Edge(VIDEO.getId(), PHOTO.getId(), Emotion.OMG)));
    Scene video =
        new Scene(ID_2, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO, VIDEO);
    mRespondedScenes = Arrays.asList(evolving, video);
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    assertSceneDisplayed(evolving, VIDEO.getId());
    final ScenesPagerFragment scenesPagerFragment = getScenesPagerFragment();
    // Wait for reaction detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            scenesPagerFragment.getCurrentFragment().getViewModel()));
      }
    });
    mFakeReactionDetectionManager.onReactionDetected(Emotion.OMG, true);
    // Should display the follow up media
    assertSceneDisplayed(evolving, PHOTO.getId());
    // Navigate to repertoire, so that theater fragment will be destroyed.
    // Click on repertoire icon
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarRepertoire.performClick();
      }
    });
    waitForMainFragment(MainActivity.TOOLBAR_REPERTOIRE_INDEX);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertNull(mMainActivityRule.getActivity().mTheaterFragment.getView());
      }
    });
    // Go back to theater
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        mMainActivityRule.getActivity().mToolbarTheater.performClick();
      }
    });
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    // Should resume to display the evolving scene in its initial state.
    assertSceneDisplayed(evolving, VIDEO.getId());
    // Should save the video scene as well
    assertEquals(video, getScenesPagerFragment().getViewModel().mItems.get(1));
  }

  @Test public void reactionDetection() throws Exception {
    Scene scene = new Scene(ID_1, DIRECTOR, HAPPY_REACTIONS, HOUR_AGO, PHOTO);
    mRespondedScenes = Collections.singletonList(scene);
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    assertSceneDisplayed(scene, PHOTO.getId());
    final ScenesPagerFragment scenesPagerFragment = getScenesPagerFragment();
    // Wait for reaction detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            scenesPagerFragment.getCurrentFragment().getViewModel()));
      }
    });
    mFakeReactionDetectionManager.onReactionDetected(Emotion.OMG, true);
    @SuppressWarnings("ConstantConditions") final ImageView reactionImage =
        scenesPagerFragment.getCurrentFragment().getView().findViewById(R.id.reactionImage);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(CameraTestUtil.areDrawablesIdentical(
            ContextCompat.getDrawable(mTestActivityRule.getActivity(),
                Emotion.OMG.getDrawableResource()), reactionImage.getDrawable()));
      }
    });
  }

  @Test public void faceDetection() throws Exception {
    Scene scene = new Scene(ID_1, DIRECTOR, HAPPY_REACTIONS, HOUR_AGO, PHOTO);
    mRespondedScenes = Collections.singletonList(scene);
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    assertSceneDisplayed(scene, PHOTO.getId());
    final ScenesPagerFragment scenesPagerFragment = getScenesPagerFragment();
    // Wait for reaction detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            scenesPagerFragment.getCurrentFragment().getViewModel()));
      }
    });
    mFakeReactionDetectionManager.onFaceDetectionStarted();
    @SuppressWarnings("ConstantConditions") final ConstraintLayout reactionsLayout =
        scenesPagerFragment.getCurrentFragment().getView().findViewById(R.id.reactionsCountLayout);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(1, reactionsLayout.getAlpha(), 0.01);
      }
    });
  }

  @Test public void evolvingScene() throws Exception {
    Scene scene = new Scene(ID_1, DIRECTOR, HAPPY_REACTIONS, HOUR_AGO, Arrays.asList(VIDEO, PHOTO),
        Collections.singletonList(new Edge(VIDEO.getId(), PHOTO.getId(), Emotion.OMG)));
    mRespondedScenes = Collections.singletonList(scene);
    mMainActivityRule.launchActivity(null);
    waitForMainFragment(MainActivity.TOOLBAR_THEATER_INDEX);
    assertSceneDisplayed(scene, VIDEO.getId());
    final ScenesPagerFragment scenesPagerFragment = getScenesPagerFragment();
    // Wait for reaction detection to start
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mFakeReactionDetectionManager.isSubscribed(
            scenesPagerFragment.getCurrentFragment().getViewModel()));
      }
    });
    mFakeReactionDetectionManager.onReactionDetected(Emotion.OMG, true);
    assertSceneDisplayed(scene, PHOTO.getId());
  }
}