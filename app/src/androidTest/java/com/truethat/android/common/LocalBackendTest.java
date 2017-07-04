package com.truethat.android.common;

import android.support.test.espresso.action.ViewActions;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.empathy.MockReactionDetectionModule;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import com.truethat.android.ui.activity.BaseActivity;
import com.truethat.android.ui.activity.OnBoardingActivity;
import com.truethat.android.ui.activity.OnBoardingActivityTest;
import com.truethat.android.ui.activity.RepertoireActivity;
import com.truethat.android.ui.activity.StudioActivity;
import com.truethat.android.ui.activity.TestActivity;
import com.truethat.android.ui.activity.TheaterActivity;
import java.util.Date;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.truethat.android.application.ApplicationTestUtil.centerSwipeUp;
import static com.truethat.android.application.ApplicationTestUtil.getCurrentActivity;
import static com.truethat.android.application.ApplicationTestUtil.isDebugging;
import static com.truethat.android.application.ApplicationTestUtil.launchApp;
import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static com.truethat.android.common.BaseApplicationTestSuite.TIMEOUT;
import static com.truethat.android.model.User.LAST_USER_PATH;
import static com.truethat.android.ui.activity.ReactablesPagerActivityTest.assertReactableDisplayed;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

@MediumTest public class LocalBackendTest {
  /**
   * Default duration to wait for. When waiting for activities to change for example.
   */
  private static final String FIRST_NAME = "Pablo Escobar";
  private static final String SECOND_NAME = "Gustavo Gaviria";
  private static final Emotion REACTION = Emotion.HAPPY;
  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  private MockInternalStorage mMockInternalStorage;
  private MockReactionDetectionModule mMockReactionDetectionModule;

  @Before public void setUp() throws Exception {
    TIMEOUT = isDebugging() ? Duration.ONE_MINUTE : Duration.TEN_SECONDS;
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(TIMEOUT);
    // Sets up the mocked permissions module.
    App.setPermissionsModule(new MockPermissionsModule(Permission.CAMERA, Permission.PHONE));
    // Sets up the mocked in-mem internal storage.
    App.setInternalStorage(mMockInternalStorage = new MockInternalStorage());
    // Sets up a mocked emotional reaction detection module.
    App.setReactionDetectionModule(
        mMockReactionDetectionModule = new MockReactionDetectionModule());
  }

  /**
   * Test an interaction between two users. The first {@link User} uploads a {@link Scene} via
   * {@link StudioActivity}. Whereas the second {@link User} views it in {@link TheaterActivity}
   * and reacts to it. Finally, the first user receives the reaction in {@link RepertoireActivity}.
   * And so, we have the following stages:
   * <ul>
   * <li>Create first {@link User} in {@link OnBoardingActivity}.</li>
   * <li>Navigate to {@link StudioActivity}.</li>
   * <li>Direct a {@link Scene} and upload it.</li>
   * <li>Assert {@link TheaterActivity} does not display it.</li>
   * <li>Sign out.</li>
   * <li>Create second {@link User} in {@link OnBoardingActivity}.</li>
   * <li>View the created {@link Reactable} in {@link TheaterActivity}.</li>
   * <li>React to it.</li>
   * <li>Sign out.</li>
   * <li>Log in as the first {@link User}, without navigating through {@link
   * OnBoardingActivity}.</li>
   * <li>Navigate to {@link RepertoireActivity}.</li>
   * <li>See the second {@link User} reaction.</li>
   * </ul>
   */
  @Test public void basicFlow() throws Exception {
    // Sets up first device manager for the first user.
    MockDeviceManager firstDeviceManager = new MockDeviceManager();
    firstDeviceManager.setDeviceId("sod-kamus-" + new Date().getTime());
    firstDeviceManager.setPhoneNumber("+111-" + new Date().getTime());
    App.setDeviceManager(firstDeviceManager);
    // Launches the app.
    launchApp();
    // Should navigate to On-Boarding
    waitForActivity(OnBoardingActivity.class);
    // Do on boarding for first user.
    OnBoardingActivityTest.doOnBoarding(FIRST_NAME, mMockReactionDetectionModule);
    // Should navigate to Theater Activity after on boarding.
    waitForActivity(TheaterActivity.class);
    // Saves the first user.
    User firstUser = User.getUserFromStorage(getCurrentActivity());
    // Should not have any reactables to display.
    waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText)));
    // Navigate to studio
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
    // Take a picture
    onView(withId(R.id.captureButton)).perform(click());
    // Wait for approval
    waitMatcher(allOf(withId(R.id.sendButton), isDisplayed()));
    // Store studio activity.
    final StudioActivity studioActivity = (StudioActivity) getCurrentActivity();
    // Approve scene
    onView(withId(R.id.sendButton)).perform(click());
    // Should navigate to theater upon publish
    waitForActivity(TheaterActivity.class);
    // Reactable should have an id and an image url.
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        Scene scene = (Scene) studioActivity.getDirectedReactable();
        assertTrue(scene.hasId());
        assertNotNull(scene.getImageSignedUrl());
      }
    });
    Scene scene = (Scene) studioActivity.getDirectedReactable();
    // Navigate to repertoire.
    centerSwipeUp();
    waitForActivity(StudioActivity.class);
    centerSwipeUp();
    waitForActivity(RepertoireActivity.class);
    // Should display the created scene.
    assertReactableDisplayed(scene);
    // Sign out.
    App.getAuthModule().signOut();
    // Relaunch app.
    launchApp();
    // Delete user from internal storage.
    mMockInternalStorage.delete(null, LAST_USER_PATH);
    // Sets up device manager for the second user.
    MockDeviceManager secondDeviceManager = new MockDeviceManager();
    secondDeviceManager.setDeviceId("this-is-who-i-am-" + new Date().getTime());
    secondDeviceManager.setPhoneNumber("+222-" + new Date().getTime());
    App.setDeviceManager(secondDeviceManager);
    // Start on boarding again
    App.getAuthModule().auth((BaseActivity) getCurrentActivity());
    waitForActivity(OnBoardingActivity.class);
    // Do on boarding for the second user.
    OnBoardingActivityTest.doOnBoarding(SECOND_NAME, mMockReactionDetectionModule);
    // Should navigate to Theater Activity after on boarding.
    waitForActivity(TheaterActivity.class);
    // Should display the scene directed by the first user
    assertReactableDisplayed(scene);
    // Should detect reactions.
    assertTrue(mMockReactionDetectionModule.isDetecting());
    // React to the scene.
    mMockReactionDetectionModule.doDetection(REACTION);
    scene.doReaction(REACTION);
    // Should affect UI:
    onView(withId(R.id.reactionCountText)).check(matches(withText("1")));
    // Let events be posted.
    Thread.sleep(TIMEOUT.getValueInMS() / 2);
    // Sign out.
    App.getAuthModule().signOut();
    // Sets up first device manager for the first user.
    App.setDeviceManager(firstDeviceManager);
    // Save first user to internal storage.
    firstUser.save(getCurrentActivity());
    // Relaunch app.
    launchApp();
    // Auth again
    App.getAuthModule().auth((BaseActivity) getCurrentActivity());
    // Should remain in Theater Activity
    waitForActivity(TheaterActivity.class);
    // Navigate to repertoire.
    onView(withId(R.id.activityRootView)).perform(ViewActions.swipeUp());
    waitForActivity(StudioActivity.class);
    centerSwipeUp();
    waitForActivity(RepertoireActivity.class);
    // Should display the scene. Note the updated reaction counter.
    assertReactableDisplayed(scene);
  }
}
