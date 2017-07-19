package com.truethat.android.view.activity;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.CountingDispatcher;
import com.truethat.android.model.Emotion;
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

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 */
public class RepertoireActivityTest extends BaseApplicationTestSuite {
  @Rule public ActivityTestRule<RepertoireActivity> mRepertoireActivityTestRule =
      new ActivityTestRule<>(RepertoireActivity.class, true, false);
  private List<Scene> mRespondedScenes;

  @Override public void setUp() throws Exception {
    super.setUp();
    setDispatcher(new CountingDispatcher() {
      @Override public MockResponse processRequest(RecordedRequest request) throws Exception {
        String responseBody = mGson.toJson(mRespondedScenes);
        mRespondedScenes = Collections.emptyList();
        return new MockResponse().setBody(responseBody);
      }
    });
    // By default the scenes list is empty.
    mRespondedScenes = Collections.emptyList();
  }

  @Test public void navigation() throws Exception {
    mRepertoireActivityTestRule.launchActivity(null);
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(StudioActivity.class);
  }

  @Test public void navigationWhileReactableDisplayed() throws Exception {
    Scene scene = new Scene(1L, "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg",
        mFakeAuthManager.currentUser(), new TreeMap<Emotion, Long>(), new Date(), null);
    mRespondedScenes = Collections.singletonList(scene);
    mRepertoireActivityTestRule.launchActivity(null);
    ReactablesPagerActivityTest.assertReactableDisplayed(scene, mFakeAuthManager.currentUser());
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(StudioActivity.class);
  }

  @Test public void singleInstance() throws Exception {
    Scene scene = new Scene(1L, "http://i.huffpost.com/gen/1226293/thumbs/o-OBAMA-LAUGHING-570.jpg",
        mFakeAuthManager.currentUser(), new TreeMap<Emotion, Long>(), new Date(), null);
    mRespondedScenes = Collections.singletonList(scene);
    mRepertoireActivityTestRule.launchActivity(null);
    ReactablesPagerActivityTest.assertReactableDisplayed(scene, mFakeAuthManager.currentUser());
    // Navigate out of Repertoire activity
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeDown());
    waitForActivity(StudioActivity.class);
    // Navigate back to Repertoire activity
    centerSwipeUp();
    waitForActivity(RepertoireActivity.class);
    ReactablesPagerActivityTest.assertReactableDisplayed(scene, mFakeAuthManager.currentUser());
  }
}