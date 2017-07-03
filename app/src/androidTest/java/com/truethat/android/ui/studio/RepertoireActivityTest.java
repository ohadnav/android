package com.truethat.android.ui.studio;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import com.truethat.android.R;
import com.truethat.android.application.auth.MockAuthModule;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.InteractionAPI;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.CameraTestUtil;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Scene;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
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
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.truethat.android.application.ApplicationTestUtil.isFullScreen;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
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
public class RepertoireActivityTest extends BaseApplicationTestSuite {
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
  @Rule public ActivityTestRule<RepertoireActivity> mRepertoireActivityRule =
      new ActivityTestRule<>(RepertoireActivity.class, true, false);
  private List<Scene> mRespondedScenes;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Resets the post event counter.
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = NetworkUtil.GSON.toJson(mRespondedScenes) + "\n";
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
  }

  @Test public void navigation() throws Exception {
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(StudioActivity.class);
  }

  @Test public void displayReactable() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, MockAuthModule.USER, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mRepertoireActivityRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.reactableFragment)));
    // Asserting the layout is displayed fullscreen.
    onView(withId(R.id.reactableFragment)).check(matches(isFullScreen()));
    // Loading layout should be hidden.
    onView(withId(R.id.loadingLayout)).check(matches(not(isDisplayed())));
    // Asserting the reactions count is abbreviated.
    onView(withId(R.id.reactionCountText)).check(
        matches(withText(NumberUtil.format(HAPPY_COUNT + SAD_COUNT))));
    // Asserting the reaction image is displayed and represents the most common reaction.
    onView(withId(R.id.reactionImage)).check(matches(isDisplayed()));
    ImageView imageView =
        (ImageView) mRepertoireActivityRule.getActivity().findViewById(R.id.reactionImage);
    assertTrue(CameraTestUtil.areDrawablesIdentical(imageView.getDrawable(),
        ContextCompat.getDrawable(mRepertoireActivityRule.getActivity().getApplicationContext(),
            EMOTIONAL_REACTIONS.lastKey().getDrawableResource())));
    // Asserting the scene image is displayed fullscreen.
    onView(withId(R.id.sceneImage)).check(matches(isFullScreen()));
    // Director name should be hidden
    onView(withId(R.id.directorNameText)).check(matches(not(isDisplayed())));
    // Asserting the displayed time is represents the reactable creation.
    onView(withId(R.id.timeAgoText)).check(matches(withText(DateUtil.formatTimeAgo(HOUR_AGO))));
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
        String responseBody = NetworkUtil.GSON.toJson(mRespondedScenes) + "\n";
        Thread.sleep(BaseApplicationTestSuite.TIMEOUT.getValueInMS() / 2);
        return new MockResponse().setBody(responseBody);
      }
    });
    mRepertoireActivityRule.launchActivity(null);
    // Wait until not found text is displayed
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText))));
    // A reactable should not be displayed.
    onView(withId(R.id.reactableFragment)).check(doesNotExist());
    // Explicitly load more reactables.
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, MockAuthModule.USER, new TreeMap<Emotion, Long>(), HOUR_AGO,
            Emotion.HAPPY));
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
    onView(isRoot()).perform(waitMatcher(withId(R.id.reactableFragment)));
    // Loading layout should be hidden.
    onView(withId(R.id.loadingLayout)).check(matches(not(isDisplayed())));
  }

  @Test public void noReactablesFound_failedRequest() throws Exception {
    mMockWebServer.close();
    mRepertoireActivityRule.launchActivity(null);
    // Not found text should be displayed.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText))));
  }

  @Test public void noReactablesFound_failedResponse() throws Exception {
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    });
    mRepertoireActivityRule.launchActivity(null);
    // Not found text should be displayed.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText))));
  }

  // Test is unstable, run individually if needed.
  @Test public void nextReactable() throws Exception {
    mRespondedScenes = Arrays.asList(
        new Scene(ID_1, IMAGE_URL_1, MockAuthModule.USER, EMOTIONAL_REACTIONS, HOUR_AGO, null),
        new Scene(ID_2, IMAGE_URL_2, MockAuthModule.USER, EMOTIONAL_REACTIONS, YESTERDAY, null));
    mRepertoireActivityRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.reactableFragment)));
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Wait until the next reactable is displayed, or throw otherwise.
    onView(isRoot()).perform(
        waitMatcher(allOf(withId(R.id.timeAgoText), withText(DateUtil.formatTimeAgo(YESTERDAY)))));
  }

  @Test public void previousReactable() throws Exception {
    mRespondedScenes = Arrays.asList(
        new Scene(ID_1, IMAGE_URL_1, MockAuthModule.USER, EMOTIONAL_REACTIONS, HOUR_AGO, null),
        new Scene(ID_2, IMAGE_URL_2, MockAuthModule.USER, EMOTIONAL_REACTIONS, YESTERDAY, null));
    mRepertoireActivityRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.reactableFragment)));
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
    // Wait until the next reactable is displayed, or throw otherwise.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.timeAgoText),
        withText(DateUtil.formatTimeAgo(YESTERDAY)))));
    // Triggers navigation to previous reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeRight());
    // Wait until the next reactable is displayed, or throw otherwise.
    onView(isRoot()).perform(
        waitMatcher(allOf(withId(R.id.timeAgoText), withText(DateUtil.formatTimeAgo(HOUR_AGO)))));
    // Triggers navigation to previous reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeRight());
    // Makes sure reactable is unchanged
    onView(allOf(isDisplayed(), withId(R.id.timeAgoText),
        withText(DateUtil.formatTimeAgo(HOUR_AGO)))).check(matches(isDisplayed()));
  }

  @Test public void nextReactableFetchesNewReactables() throws Exception {
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_1, IMAGE_URL_1, MockAuthModule.USER, EMOTIONAL_REACTIONS, HOUR_AGO, null));
    mRepertoireActivityRule.launchActivity(null);
    // Wait until the reactable is displayed.
    onView(isRoot()).perform(waitMatcher(withId(R.id.reactableFragment)));
    // Updates responded scenes.
    mRespondedScenes = Collections.singletonList(
        new Scene(ID_2, IMAGE_URL_2, MockAuthModule.USER, EMOTIONAL_REACTIONS, YESTERDAY, null));
    // Triggers navigation to next reactable.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeLeft());
  }
}