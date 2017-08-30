package com.truethat.android.view.fragment;

import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Short;
import com.truethat.android.view.activity.RepertoireActivity;
import java.util.Collections;
import java.util.Date;
import java.util.TreeMap;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.common.network.NetworkUtil.GSON;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/08/2017 for TrueThat.
 */
public class ShortFragmentTest extends BaseApplicationTestSuite {
  @Rule public ActivityTestRule<RepertoireActivity> mRepertoireActivityRule =
      new ActivityTestRule<>(RepertoireActivity.class, true, false);
  private Short mShort;
  private ShortFragment mShortFragment;

  @Override public void setUp() throws Exception {
    super.setUp();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = GSON.toJson(Collections.singletonList(mShort));
        return new MockResponse().setBody(responseBody);
      }
    });
    mShort =
        new Short(1L, mFakeAuthManager.getCurrentUser(), new TreeMap<Emotion, Long>(), new Date(),
            null,
            "https://storage.googleapis.com/truethat-test-studio/testing/Ohad_wink_compressed.mp4");
    mRepertoireActivityRule.launchActivity(null);
    // Wait until the reactable fragment is created.
    waitMatcher(withId(R.id.reactableFragment));
    mShortFragment =
        (ShortFragment) ((ReactablesPagerFragment) getCurrentActivity().getSupportFragmentManager()
            .findFragmentById(R.id.reactablesPagerFragment)).getDisplayedReactable();
    // Wait until the fragment is ready
    await().atMost(Duration.FIVE_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mShortFragment.getViewModel().isReady());
      }
    });
    // Loading image should be hidden once ready
    waitMatcher(allOf(withId(R.id.loadingImage), not(isDisplayed())));
  }

  @Test public void pauseAndResumeWithTouch() throws Exception {
    // Should be playing video
    await().atMost(Duration.FIVE_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mShortFragment.getMediaPlayer().isPlaying());
        assertNotEquals(0, mShortFragment.getMediaPlayer().getCurrentPosition());
      }
    });
    // Pause video
    final int currentPosition = mShortFragment.getMediaPlayer().getCurrentPosition();
    onView(withId(R.id.videoSurface)).perform(longClick());
    assertTrue(mShortFragment.getMediaPlayer().getCurrentPosition() - currentPosition < 50);
    // Should resume playing
    await().atMost(Duration.FIVE_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mShortFragment.getMediaPlayer().isPlaying());
        assertTrue(mShortFragment.getMediaPlayer().getCurrentPosition() - currentPosition > 1000);
      }
    });
  }
}