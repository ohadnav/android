package com.truethat.android.view.activity;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Photo;
import com.truethat.android.model.Scene;
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
import static com.truethat.android.view.fragment.ScenesPagerFragmentTest.assertSceneDisplayed;

/**
 * Proudly created by ohad on 05/06/2017 for TrueThat.
 */
public class TheaterActivityTest extends BaseApplicationTestSuite {
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  private List<Scene> mRespondedScenes;
  private Scene mScene;

  @Override public void setUp() throws Exception {
    super.setUp();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = GSON.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the poses list is empty.
    mRespondedScenes = Collections.emptyList();
    mScene = new Scene(1L, mFakeAuthManager.getCurrentUser(), new TreeMap<Emotion, Long>(),
        new Date(), null,
        new Photo("http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg", null));
  }

  @Test public void navigation() throws Exception {
    mTheaterActivityTestRule.launchActivity(null);
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
  }

  @Test public void navigationWhileSceneDisplayed() throws Exception {
    mRespondedScenes = Collections.singletonList(mScene);
    mTheaterActivityTestRule.launchActivity(null);
    assertSceneDisplayed(mScene, mFakeAuthManager.getCurrentUser());
    centerSwipeUp();
    waitForActivity(StudioActivity.class);
  }

  @Test public void singleInstance() throws Exception {
    mRespondedScenes = Collections.singletonList(mScene);
    mTheaterActivityTestRule.launchActivity(null);
    assertSceneDisplayed(mScene, mFakeAuthManager.getCurrentUser());
    // Navigate out of Theater activity
    centerSwipeUp();
    waitForActivity(StudioActivity.class);
    // Navigate back to Theater activity
    centerSwipeUp();
    waitForActivity(TheaterActivity.class);
    assertSceneDisplayed(mScene, mFakeAuthManager.getCurrentUser());
  }
}