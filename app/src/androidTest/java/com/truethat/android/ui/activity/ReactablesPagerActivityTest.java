package com.truethat.android.ui.activity;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.InteractionAPI;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.CameraTestUtil;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.ui.common.media.ReactableFragment;
import java.net.HttpURLConnection;
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
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isFullscreen;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 26/06/2017 for TrueThat.
 */
public class ReactablesPagerActivityTest extends BaseApplicationTestSuite {
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
  @Rule public ActivityTestRule<RepertoireActivity> mRepertoireActivityRule =
      new ActivityTestRule<>(RepertoireActivity.class, true, false);
  private List<Scene> mRespondedScenes;

  @SuppressWarnings("ConstantConditions")
  public static void assertReactableDisplayed(final Reactable reactable) throws Exception {
    final ReactablesPagerActivity pagerActivity = (ReactablesPagerActivity) getCurrentActivity();
    // Wait until the reactable is displayed.
    waitMatcher(withId(R.id.reactableFragment));
    // Wait until the correct fragment is the current one.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(reactable.getId(),
            pagerActivity.getDisplayedReactable().getReactable().getId());
      }
    });
    final ReactableFragment currentFragment = pagerActivity.getDisplayedReactable();
    // Wait until the fragment is really visible.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(currentFragment.isReady());
      }
    });
    // Asserting the scene image is displayed fullscreen.
    assertTrue(isFullscreen(currentFragment.getView().findViewById(R.id.sceneImage)));
    // Loading layout should be hidden.
    onView(withId(R.id.loadingLayout)).check(matches(not(isDisplayed())));
    // Asserting the reactions count is abbreviated.
    assertEquals(NumberUtil.format(NumberUtil.sum(reactable.getReactionCounters())),
        ((TextView) currentFragment.getView().findViewById(R.id.reactionCountText)).getText());
    // Asserting the reaction image is displayed and represents the most common reaction, the user reaction, or the default one.
    onView(allOf(isDisplayed(), withId(R.id.reactionImage))).check(matches(isDisplayed()));
    ImageView reactionImage =
        (ImageView) currentFragment.getView().findViewById(R.id.reactionImage);
    if (currentFragment.getReactable().getUserReaction() != null) {
      assertTrue(CameraTestUtil.areDrawablesIdentical(reactionImage.getDrawable(),
          ContextCompat.getDrawable(pagerActivity.getApplicationContext(),
              currentFragment.getReactable().getUserReaction().getDrawableResource())));
    } else if (!reactable.getReactionCounters().isEmpty()) {
      assertTrue(CameraTestUtil.areDrawablesIdentical(reactionImage.getDrawable(),
          ContextCompat.getDrawable(pagerActivity.getApplicationContext(),
              reactable.getReactionCounters().lastKey().getDrawableResource())));
    } else {
      assertTrue(CameraTestUtil.areDrawablesIdentical(reactionImage.getDrawable(),
          ContextCompat.getDrawable(pagerActivity.getApplicationContext(),
              ReactableFragment.DEFAULT_REACTION_COUNTER.getDrawableResource())));
    }
    // Asserting the displayed time is represents the reactable creation.
    assertEquals(DateUtil.formatTimeAgo(reactable.getCreated()),
        ((TextView) currentFragment.getView().findViewById(R.id.timeAgoText)).getText());
    // Should not display director name if the current user is the director.
    if (reactable.getDirector().getId() != App.getAuthModule().getUser().getId()) {
      assertEquals(View.VISIBLE,
          currentFragment.getView().findViewById(R.id.directorNameText).getVisibility());
      // Asserting the displayed name is of the reactable director
      assertEquals(reactable.getDirector().getDisplayName(),
          ((TextView) currentFragment.getView().findViewById(R.id.directorNameText)).getText());
    } else {
      assertEquals(View.GONE,
          currentFragment.getView().findViewById(R.id.directorNameText).getVisibility());
    }
  }

  @Before public void setUp() throws Exception {
    super.setUp();
    // Resets the post event counter.
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = NetworkUtil.GSON.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
    // Authenticate the user.
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return App.getAuthModule().isAuthOk();
      }
    });
  }

  @Test public void displayReactable() throws Exception {
    Scene scene =
        new Scene(ID_1, IMAGE_URL_1, App.getAuthModule().getUser(), EMOTIONAL_REACTIONS, HOUR_AGO,
            null);
    mRespondedScenes = Collections.singletonList(scene);
    mRepertoireActivityRule.launchActivity(null);
    assertReactableDisplayed(scene);
    // Should not be detecting reaction
    assertFalse(mMockReactionDetectionModule.isDetecting());
    // Let a post event to maybe be sent.
    Thread.sleep(BaseApplicationTestSuite.TIMEOUT.getValueInMS() / 2);
    assertEquals(0, mDispatcher.getCount(InteractionAPI.PATH));
  }

  @Test public void noReactablesFound() throws Exception {
    // A new dispatcher is set to ensure asynchronous server behaviour, so that we can test non
    // found text proper display.
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = NetworkUtil.GSON.toJson(mRespondedScenes);
        Thread.sleep(BaseApplicationTestSuite.TIMEOUT.getValueInMS() / 2);
        return new MockResponse().setBody(responseBody);
      }
    });
    mRepertoireActivityRule.launchActivity(null);
    // Wait until not found text is displayed
    waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText)));
    // A reactable should not be displayed.
    onView(withId(R.id.reactableFragment)).check(doesNotExist());
    Scene scene =
        new Scene(ID_1, IMAGE_URL_1, App.getAuthModule().getUser(), new TreeMap<Emotion, Long>(),
            HOUR_AGO, Emotion.HAPPY);
    // Explicitly load more reactables.
    mRespondedScenes = Collections.singletonList(scene);
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Not found text should be hidden.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        // Fragment is not yet shown
        onView(withId(R.id.reactableFragment)).check(doesNotExist());
        // Not found text is hidden
        onView(withId(R.id.notFoundText)).check(matches(not(isDisplayed())));
      }
    });
    // Wait until the reactable is displayed.
    assertReactableDisplayed(scene);
  }

  @Test public void noReactablesFound_failedRequest() throws Exception {
    mMockWebServer.close();
    mRepertoireActivityRule.launchActivity(null);
    // Not found text should be displayed.
    waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText)));
  }

  @Test public void noReactablesFound_failedResponse() throws Exception {
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    mRepertoireActivityRule.launchActivity(null);
    // Not found text should be displayed.
    waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText)));
  }

  @Test public void nextReactable() throws Exception {
    Scene scene1 =
        new Scene(ID_1, IMAGE_URL_1, App.getAuthModule().getUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    Scene scene2 =
        new Scene(ID_2, IMAGE_URL_2, App.getAuthModule().getUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            null);
    mRespondedScenes = Arrays.asList(scene1, scene2);
    mRepertoireActivityRule.launchActivity(null);
    // First reactable should be displayed.
    assertReactableDisplayed(scene1);
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Second reactable should be displayed.
    assertReactableDisplayed(scene2);
  }

  @Test public void previousReactable() throws Exception {
    Scene scene1 =
        new Scene(ID_1, IMAGE_URL_1, App.getAuthModule().getUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    Scene scene2 =
        new Scene(ID_2, IMAGE_URL_2, App.getAuthModule().getUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            null);
    mRespondedScenes = Arrays.asList(scene1, scene2);
    mRepertoireActivityRule.launchActivity(null);
    // First reactable should be displayed.
    assertReactableDisplayed(scene1);
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Second reactable should be displayed.
    assertReactableDisplayed(scene2);
    // Triggers navigation to previous reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeRight());
    // First reactable should be displayed.
    assertReactableDisplayed(scene1);
    // Triggers navigation to previous reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeRight());
    // First reactable should still be displayed.
    assertReactableDisplayed(scene1);
  }

  @Test public void nextReactableFetchesNewReactables() throws Exception {
    Scene scene1 =
        new Scene(ID_1, IMAGE_URL_1, App.getAuthModule().getUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    Scene scene2 =
        new Scene(ID_2, IMAGE_URL_2, App.getAuthModule().getUser(), EMOTIONAL_REACTIONS, YESTERDAY,
            null);
    mRespondedScenes = Collections.singletonList(scene1);
    mRepertoireActivityRule.launchActivity(null);
    // First reactable should be displayed.
    assertReactableDisplayed(scene1);
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(scene2);
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Second reactable should be displayed.
    assertReactableDisplayed(scene2);
  }
}