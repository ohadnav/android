package com.truethat.android.view.fragment;

import android.support.test.rule.ActivityTestRule;
import android.view.ViewConfiguration;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Scene;
import com.truethat.android.model.Video;
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
import static com.truethat.android.view.fragment.ScenesPagerFragmentTest.assertSceneDisplayed;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 24/08/2017 for TrueThat.
 */
public class VideoFragmentTest extends BaseApplicationTestSuite {
  @Rule public ActivityTestRule<RepertoireActivity> mRepertoireActivityRule =
      new ActivityTestRule<>(RepertoireActivity.class, true, false);
  // TODO(ohad): remove dependency on external activity basically
  private VideoFragment mVideoFragment;

  @Test public void pauseAndResumeWithTouch() throws Exception {
    final Scene scene =
        new Scene(1L, mFakeAuthManager.getCurrentUser(), new TreeMap<Emotion, Long>(), new Date(),
            new Video(
                "https://storage.googleapis.com/truethat-test-studio/testing/Ohad_wink_compressed.mp4",
                null));
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = GSON.toJson(Collections.singletonList(scene));
        return new MockResponse().setBody(responseBody);
      }
    });
    mRepertoireActivityRule.launchActivity(null);
    assertSceneDisplayed(scene, mFakeAuthManager.getCurrentUser(), 0);
    mVideoFragment =
        (VideoFragment) ((ScenesPagerFragment) getCurrentActivity().getSupportFragmentManager()
            .findFragmentById(R.id.scenesPagerFragment)).getDisplayedScene().getMediaFragment();
    // Loading image should be hidden once ready
    waitMatcher(allOf(withId(R.id.loadingImage), not(isDisplayed())));
    // Should be playing video
    await().atMost(Duration.FIVE_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mVideoFragment.getMediaPlayer().isPlaying());
        assertNotEquals(0, mVideoFragment.getMediaPlayer().getCurrentPosition());
      }
    });
    // Pause video
    final int currentPosition = mVideoFragment.getMediaPlayer().getCurrentPosition();
    onView(withId(R.id.videoSurface)).perform(longClick());
    assertTrue(mVideoFragment.getMediaPlayer().getCurrentPosition() - currentPosition
        < ViewConfiguration.getLongPressTimeout() / 2);
    // Should resume playing
    await().atMost(Duration.FIVE_SECONDS).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(mVideoFragment.getMediaPlayer().isPlaying());
        assertTrue(mVideoFragment.getMediaPlayer().getCurrentPosition() - currentPosition > 1000);
      }
    });
  }
}