package com.truethat.android.common;

import android.support.test.rule.ActivityTestRule;
import com.truethat.android.application.App;
import com.truethat.android.application.ApplicationTestUtil;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.empathy.MockReactionDetectionModule;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import com.truethat.android.ui.common.TestActivity;
import com.truethat.android.ui.studio.RepertoireActivity;
import com.truethat.android.ui.studio.StudioActivity;
import com.truethat.android.ui.theater.TheaterActivity;
import com.truethat.android.ui.welcome.OnBoardingActivity;
import com.truethat.android.ui.welcome.OnBoardingActivityTest;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.truethat.android.application.ApplicationTestUtil.waitForActivity;

/**
 * Proudly created by ohad on 02/07/2017 for TrueThat.
 */

public class LocalBackendTest {
  /**
   * Default duration to wait for. When waiting for activities to change for example.
   */
  public static final Duration TIMEOUT =
      ApplicationTestUtil.isDebugging() ? Duration.ONE_MINUTE : Duration.TEN_SECONDS;
  private static final String FIRST_NAME = "Pablo Escobar";
  private static final String SECOND_NAME = "Gustavo Gaviria";
  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  protected MockPermissionsModule mMockPermissionsModule;
  protected MockInternalStorage mMockInternalStorage;
  protected MockReactionDetectionModule mMockReactionDetectionModule;
  protected MockDeviceManager mMockDeviceManager;

  @Before public void setUp() throws Exception {
    // Initialize Awaitility
    Awaitility.reset();
    Awaitility.setDefaultTimeout(TIMEOUT);
    // Sets up the mocked permissions module.
    App.setPermissionsModule(mMockPermissionsModule = new MockPermissionsModule());
    // Sets up the mocked in-mem internal storage.
    App.setInternalStorage(mMockInternalStorage = new MockInternalStorage());
    // Sets up a mocked emotional reaction detection module.
    App.setReactionDetectionModule(
        mMockReactionDetectionModule = new MockReactionDetectionModule());
    // Sets up mocked device manager.
    App.setDeviceManager(mMockDeviceManager = new MockDeviceManager());
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
    // Should navigate to On-Boarding
    waitForActivity(OnBoardingActivity.class);
    OnBoardingActivityTest.doOnBoarding(FIRST_NAME, mMockReactionDetectionModule);
    // Should navigate to Theater Activity after on boarding.
    waitForActivity(TheaterActivity.class);
    // Should not have any reactables to display.

  }
}
