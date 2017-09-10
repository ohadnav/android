package com.truethat.android.view.activity;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Reactable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.centerSwipeUp;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.common.network.NetworkUtil.GSON;
import static com.truethat.android.view.fragment.ReactablesPagerFragmentTest.assertReactableDisplayed;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */
public class TheaterActivityTest extends BaseApplicationTestSuite {
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  private List<Reactable> mRespondedReactables;
  private Reactable mReactable;

  @Override public void setUp() throws Exception {
    super.setUp();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = GSON.toJson(mRespondedReactables);
        mRespondedReactables = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the poses list is empty.
    mRespondedReactables = Collections.emptyList();
    mReactable = new Reactable(1L, mFakeAuthManager.getCurrentUser(), new TreeMap<Emotion, Long>(),
        new Date(), null,
        new Photo("http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg", null));
  }

  @Test public void navigation() throws Exception {
    mTheaterActivityTestRule.launchActivity(null);
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
  }

  @Test public void navigationWhileReactableDisplayed() throws Exception {
    mRespondedReactables = Collections.singletonList(mReactable);
    mTheaterActivityTestRule.launchActivity(null);
    assertReactableDisplayed(mReactable, mFakeAuthManager.getCurrentUser());
    centerSwipeUp();
    waitForActivity(StudioActivity.class);
  }

  @Test public void singleInstance() throws Exception {
    mRespondedReactables = Collections.singletonList(mReactable);
    mTheaterActivityTestRule.launchActivity(null);
    assertReactableDisplayed(mReactable, mFakeAuthManager.getCurrentUser());
    // Navigate out of Theater activity
    centerSwipeUp();
    waitForActivity(StudioActivity.class);
    // Navigate back to Theater activity
    centerSwipeUp();
    waitForActivity(TheaterActivity.class);
    assertReactableDisplayed(mReactable, mFakeAuthManager.getCurrentUser());
  }
}