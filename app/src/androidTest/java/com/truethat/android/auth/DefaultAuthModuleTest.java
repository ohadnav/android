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
import java.util.concurrent.Callable;
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
import static org.awaitility.Awaitility.await;
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
  private boolean mFirstAuthRequest;

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
    waitUntilAuth();
    // Assert there was only a single call to auth API, i.e. that a call to #authSync was not made.
    assertEquals(1, mAuthRequestCount);
  }

  @Test public void authWithBadResponse() throws Exception {
    // Set up a bad response
    mRespondedUser = null;
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // Assert the user was not saved onto internal storage.
    Thread.sleep(200);
    assertFalse(
        App.getInternalStorage().exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH));
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
    waitUntilAuth();
    // Assert there was only a single call to auth API, since when no permission is granted a request should not be sent.
    assertEquals(1, mAuthRequestCount);
  }

  @Test public void authSync() throws Exception {
    mFirstAuthRequest = true;
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (Objects.equals(request.getMethod(), "POST") && request.getPath().endsWith("auth")) {
          mAuthRequestCount++;
        }
        if (mFirstAuthRequest) {
          mFirstAuthRequest = false;
          return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        // Sleep to ensure synchronous behaviour.
        Thread.sleep(100);
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
            .setBody(NetworkUtil.GSON.toJson(mRespondedUser) + "\n");
      }
    });
    // Authenticate user, should fail as per mFirstAuthRequest = true.
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // Let the request be properly handled.
    Thread.sleep(50);
    // Get user immediately
    User actual = App.getAuthModule().getUser();
    // Since it is a sync auth, the following should immediately apply.
    assertTrue(
        App.getInternalStorage().exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH));
    assertEquals(mRespondedUser.getId(), actual.getId());
    // Assert async auth was requested.
    assertEquals(2, mAuthRequestCount);
  }

  @Test public void dontAuthWhenUserAlreadyAuthenticated() throws Exception {
    mRespondedUser.save();
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // User should be immediately available.
    assertEquals(mRespondedUser.getId(), App.getAuthModule().getUser().getId());
    // Assert there wasn't an auth request.
    assertEquals(0, mAuthRequestCount);
  }

  private void waitUntilAuth() {
    // Wait until user is stored to internal storage.
    await().until(new Callable<Boolean>() {
      @Override public Boolean call() throws Exception {
        return App.getInternalStorage()
            .exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH);
      }
    });
    // Assert the current user now has an ID.
    assertEquals(mRespondedUser.getId(), App.getAuthModule().getUser().getId());
  }
}