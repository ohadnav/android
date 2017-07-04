package com.truethat.android.common;

import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.empathy.MockReactionDetectionModule;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
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
import org.junit.Ignore;
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

@MediumTest @Ignore public class LocalBackendTest {
  /**
   * Default duration to wait for. When waiting for activities to change for example.
   */
  private static final Date NOW = new Date();
  private static final String NAME_1 = "Pablo Escobar";
  private static final String NAME_2 = "Gustavo Gaviria";
  private static final String PHONE_1 = "+111-" + NOW.getTime();
  private static final String PHONE_2 = "+222-" + NOW.getTime();
  private static final String DEVICE_ID_1 = "android-1-" + NOW.getTime();
  private static final String DEVICE_ID_2 = "android-2-" + NOW.getTime();
  private static final String TAG = LocalBackendTest.class.getSimpleName();
  private static final Emotion REACTION = Emotion.HAPPY;
  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  @Rule public ActivityTestRule<TheaterActivity> mTheaterActivityTestRule =
      new ActivityTestRule<>(TheaterActivity.class, true, false);
  private MockInternalStorage mMockInternalStorage;
  private MockReactionDetectionModule mMockReactionDetectionModule;
  private MockDeviceManager mFirstDeviceManager = new MockDeviceManager(DEVICE_ID_1, PHONE_1);
  private MockDeviceManager mSecondDeviceManager = new MockDeviceManager(DEVICE_ID_2, PHONE_2);

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
    User firstUser = signUp(mFirstDeviceManager, NAME_1);
    assertNoScenesDisplayed();
    Scene scene = publishScene();
    sceneDisplayedInRepertoire(scene);
    signUp(mSecondDeviceManager, NAME_2);
    reactToScene(scene);
    signIn(mFirstDeviceManager, firstUser);
    sceneDisplayedInRepertoire(scene);
  }

  private User signUp(DeviceManager deviceManager, String name) throws Exception {
    hardSignOut();
    // Sets a new device manager to mock a new device.
    App.setDeviceManager(deviceManager);
    Log.v(TAG, "signUp - " + name);
    // Launch app
    mTheaterActivityTestRule.launchActivity(null);
    // Should navigate to On-Boarding
    waitForActivity(OnBoardingActivity.class);
    // Do on boarding for the user.
    OnBoardingActivityTest.doOnBoarding(name, mMockReactionDetectionModule);
    // Should navigate to Theater Activity after on boarding.
    waitForActivity(TheaterActivity.class);
    return User.getUserFromStorage(getCurrentActivity());
  }

  private void signIn(DeviceManager deviceManager, User user) throws Exception {
    hardSignOut();
    Log.v(TAG, "signIn - " + user.getDisplayName());
    App.setDeviceManager(deviceManager);
    user.save(getCurrentActivity());
    // Launch app
    mTheaterActivityTestRule.launchActivity(null);
    await().untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertTrue(App.getAuthModule().isAuthOk());
      }
    });
  }

  private Scene publishScene() {
    Log.v(TAG, "publishScene");
    // Navigate to studio
    centerSwipeUp();
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
    Log.v(TAG, "published scene - " + scene.getId());
    return scene;
  }

  private void hardSignOut() throws Exception {
    // Sign out.
    App.getAuthModule().signOut();
    // Delete user from internal storage.
    if (mMockInternalStorage.exists(null, LAST_USER_PATH)) {
      mMockInternalStorage.delete(null, LAST_USER_PATH);
    }
  }

  private void reactToScene(Scene scene) throws Exception {
    Log.v(TAG, "reactToScene - " + scene.getId());
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
  }

  private void sceneDisplayedInRepertoire(Scene scene) throws Exception {
    Log.v(TAG, "sceneDisplayedInRepertoire - " + scene.getId());
    // Navigate to repertoire.
    centerSwipeUp();
    waitForActivity(StudioActivity.class);
    centerSwipeUp();
    waitForActivity(RepertoireActivity.class);
    assertReactableDisplayed(scene);
  }

  private void assertNoScenesDisplayed() throws Exception {
    // Should not have any reactables to display.
    waitMatcher(allOf(isDisplayed(), withId(R.id.notFoundText)));
  }
}
