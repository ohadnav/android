package com.truethat.android.view.fragment;

import android.support.test.rule.ActivityTestRule;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.network.InteractionApi;
import com.truethat.android.common.util.CameraTestUtil;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.common.util.DateUtil;
import com.truethat.android.common.util.NumberUtil;
import com.truethat.android.databinding.FragmentReactableBinding;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Pose;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.User;
import com.truethat.android.view.activity.RepertoireActivity;
import com.truethat.android.viewmodel.ReactableViewModel;
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
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isFullscreen;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.common.network.NetworkUtil.GSON;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 26/06/2017 for TrueThat.
 */
public class ReactablesPagerFragmentTest extends BaseApplicationTestSuite {
  private static final long ID_1 = 1;
  private static final long ID_2 = 2;
  private static final String IMAGE_URL_1 =
      "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg";
  private static final Date HOUR_AGO = new Date(new Date().getTime() - TimeUnit.HOURS.toMillis(1));
  private static final long HAPPY_COUNT = 3000;
  private static final TreeMap<Emotion, Long> HAPPY_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, HAPPY_COUNT);
  }};
  @Rule public ActivityTestRule<RepertoireActivity> mRepertoireActivityRule =
      new ActivityTestRule<>(RepertoireActivity.class, true, false);
  private List<Pose> mRespondedPoses;

  @SuppressWarnings("ConstantConditions")
  public static void assertReactableDisplayed(final Reactable reactable, User currentUser)
      throws Exception {
    final ReactablesPagerFragment pagerFragment =
        (ReactablesPagerFragment) getCurrentActivity().getSupportFragmentManager()
            .findFragmentById(R.id.reactablesPagerFragment);
    // Wait until the reactable is displayed.
    waitMatcher(withId(R.id.reactableFragment));
    // Wait until the correct fragment is the current one.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(reactable.getId(),
            pagerFragment.getDisplayedReactable().getReactable().getId());
      }
    });
    @SuppressWarnings("unchecked")
    final ReactableFragment<Reactable, ReactableViewModel<Reactable>, FragmentReactableBinding>
        currentFragment = pagerFragment.getDisplayedReactable();
    // Wait until the fragment is ready
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(currentFragment.getViewModel().isReady());
      }
    });
    if (reactable instanceof Pose) {
      // Asserting the pose image is displayed fullscreen.
      assertTrue(isFullscreen(currentFragment.getView().findViewById(R.id.poseImage)));
    }
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
          ContextCompat.getDrawable(pagerFragment.getActivity().getApplicationContext(),
              currentFragment.getReactable().getUserReaction().getDrawableResource())));
    } else if (!reactable.getReactionCounters().isEmpty()) {
      assertTrue(CameraTestUtil.areDrawablesIdentical(reactionImage.getDrawable(),
          ContextCompat.getDrawable(pagerFragment.getActivity().getApplicationContext(),
              reactable.getReactionCounters().lastKey().getDrawableResource())));
    } else {
      assertTrue(CameraTestUtil.areDrawablesIdentical(reactionImage.getDrawable(),
          ContextCompat.getDrawable(pagerFragment.getActivity().getApplicationContext(),
              ReactableViewModel.DEFAULT_REACTION_COUNTER.getDrawableResource())));
    }
    // Asserting the displayed time is represents the reactable creation.
    assertEquals(DateUtil.formatTimeAgo(reactable.getCreated()),
        ((TextView) currentFragment.getView().findViewById(R.id.timeAgoText)).getText());
    // Should not display director name if the current user is the director.
    if (reactable.getDirector().getId() != currentUser.getId()) {
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
        String responseBody = GSON.toJson(mRespondedPoses);
        mRespondedPoses = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the poses list is empty.
    mRespondedPoses = Collections.emptyList();
  }

  @Test public void displayReactable() throws Exception {
    Pose pose =
        new Pose(ID_1, IMAGE_URL_1, mFakeAuthManager.getCurrentUser(), HAPPY_REACTIONS, HOUR_AGO,
            null);
    mRespondedPoses = Collections.singletonList(pose);
    mRepertoireActivityRule.launchActivity(null);
    assertReactableDisplayed(pose, mFakeAuthManager.getCurrentUser());
    // Should not be detecting reaction
    assertFalse(mFakeReactionDetectionManager.isDetecting());
    // Let a post event to maybe be sent.
    Thread.sleep(BaseApplicationTestSuite.TIMEOUT.getValueInMS() / 2);
    assertEquals(0, mDispatcher.getCount(InteractionApi.PATH));
  }
}