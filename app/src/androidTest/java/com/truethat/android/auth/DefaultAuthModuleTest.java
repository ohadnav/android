package com.truethat.android.auth;

import android.support.test.rule.ActivityTestRule;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.DefaultPermissionsModule;
import com.truethat.android.application.permissions.MockPermissionsModule;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.storage.internal.DefaultInternalStorage;
import com.truethat.android.application.storage.internal.MockInternalStorage;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.TestActivity;
import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.BuildConfig.BACKEND_URL;
import static com.truethat.android.BuildConfig.PORT;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 14/06/2017 for TrueThat.
 */
public class DefaultAuthModuleTest {
  private static MockPermissionsModule sMockPermissionsModule = new MockPermissionsModule();
  private final MockWebServer mMockWebServer = new MockWebServer();
  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, false);
  private User mRespondedUser;
  private int mAuthRequestCount;

  @BeforeClass public static void beforeClass() throws Exception {
    // Sets up the mocked permissions module.
    App.setPermissionsModule(sMockPermissionsModule);
    // Sets the backend URL, for MockWebServer.
    NetworkUtil.setBackendUrl("http://localhost");
  }

  @AfterClass public static void afterClass() throws Exception {
    App.setPermissionsModule(new DefaultPermissionsModule());
    // Restores default predefined backend url.
    NetworkUtil.setBackendUrl(BACKEND_URL);
    // Restores default internal storage.
    App.setInternalStorage(new DefaultInternalStorage());
  }

  @Before public void setUp() throws Exception {
    // Reset auth module.
    // Sets up the mocked auth module.
    App.setAuthModule(new DefaultAuthModule());
    // Sets up new mocked internal storage.
    App.setInternalStorage(new MockInternalStorage());
    // Grant phone permission.
    sMockPermissionsModule.grant(Permission.PHONE);
    // Initialize Awaitility
    Awaitility.reset();
    // Initializes the responded user.
    mRespondedUser = new MockAuthModule().getUser();
    // Resets request counter
    mAuthRequestCount = 0;
    // Starts mock server
    mMockWebServer.start(PORT);
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (Objects.equals(request.getMethod(), "POST") && request.getPath().endsWith("auth")) {
          mAuthRequestCount++;
        }
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(NetworkUtil.GSON.toJson(mRespondedUser) + "\n");
      }
    });
    // Launches activity
    mActivityTestRule.launchActivity(null);
  }

  @After public void tearDown() throws Exception {
    // Closes mock server
    mMockWebServer.close();
  }

  @Test public void auth() throws Exception {
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertSuccessfulAuth();
    // Assert a single call to auth API.
    assertEquals(1, mAuthRequestCount);
  }

  @Test public void authWithBadResponse() throws Exception {
    // Set up a bad response
    mRespondedUser = null;
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertFailedAuth();
  }

  @Test public void authWithNoPermission() throws Exception {
    // Revoke phone permission.
    sMockPermissionsModule.forbid(Permission.PHONE);
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // Should navigate to ask for permission activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.askForPermissionActivity)),
            TimeUnit.SECONDS.toMillis(1)));
    // Grant permission, to mock the scenario where the user allowed the permission.
    sMockPermissionsModule.reset(Permission.PHONE);
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).perform(click());
    // Should return to Test activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.testActivity)), TimeUnit.SECONDS.toMillis(1)));
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertSuccessfulAuth();
    // Assert there was only a single call to auth API, since when no permission is granted a request should not be sent.
    assertEquals(1, mAuthRequestCount);
  }

  // Asserts auth is synchronous.
  @Test public void authSync() throws Exception {
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (Objects.equals(request.getMethod(), "POST") && request.getPath().endsWith("auth")) {
          mAuthRequestCount++;
        }
        // Sleep to ensure synchronous behaviour.
        Thread.sleep(100);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(NetworkUtil.GSON.toJson(mRespondedUser) + "\n");
      }
    });
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // User should be immediately available, as authentication is executed synchronously.
    assertSuccessfulAuth();
  }

  @Test public void dontAuthWhenUserAlreadyAuthenticated() throws Exception {
    mRespondedUser.save(mActivityTestRule.getActivity());
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertSuccessfulAuth();
    // Assert there wasn't any auth request.
    assertEquals(0, mAuthRequestCount);
  }

  private void assertSuccessfulAuth() {
    // Assert the user was not saved onto internal storage.
    assertTrue(
        App.getInternalStorage().exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH));
    // Should navigate back to Test activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.testActivity)), TimeUnit.SECONDS.toMillis(1)));
    // Assert the current user now has an ID.
    assertEquals(mRespondedUser.getId(), App.getAuthModule().getUser().getId());
  }

  private void assertFailedAuth() {
    // Assert the user was not saved onto internal storage.
    assertFalse(
        App.getInternalStorage().exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH));
    // Should navigate to Welcome activity.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.welcomeActivity)),
        TimeUnit.SECONDS.toMillis(1)));
    // Assert the current user has no ID or is null.
    if (App.getAuthModule().getUser() != null) {
      assertFalse(App.getAuthModule().getUser().hasId());
    }
  }
}