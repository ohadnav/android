package com.truethat.android.auth;

import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.common.BaseApplicationTest;
import com.truethat.android.common.network.NetworkUtil;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.application.ApplicationTestUtil.waitMatcher;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 14/06/2017 for TrueThat.
 */
public class DefaultAuthModuleTest extends BaseApplicationTest {
  private User mRespondedUser;
  private int mAuthRequestCount;

  @Before public void setUp() throws Exception {
    super.setUp();
    // Sets up default AuthModule.
    App.setAuthModule(new DefaultAuthModule());
    // Initializes the responded user.
    mRespondedUser = MockAuthModule.USER;
    // Resets request counter
    mAuthRequestCount = 0;
    mMockWebServer.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        if (Objects.equals(request.getMethod(), "POST") && request.getPath().endsWith("auth")) {
          mAuthRequestCount++;
        }
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(mRespondedUser) + "\n");
      }
    });
    User initializedUser =
        new User(null, mRespondedUser.getFirstName(), mRespondedUser.getLastName(), null, null);
    // Mock user onBoarding.
    initializedUser.save(mActivityTestRule.getActivity());
  }

  @Test public void authOk() throws Exception {
    // Authenticate user;
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertAuthOk();
    // Assert a single call to auth API.
    assertEquals(1, mAuthRequestCount);
  }

  @Test public void authWithBadResponse() throws Exception {
    // Set up a bad response
    mRespondedUser = null;
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertAuthFailed();
  }

  @Test public void tryAgain() throws Exception {
    // Set up a bad response
    mRespondedUser = null;
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertAuthFailed();
    // Try again with proper response.
    mRespondedUser = MockAuthModule.USER;
    // Click on sign in text
    onView(withId(R.id.signInText)).check(matches(isDisplayed())).perform(click());
    assertAuthOk();
    // Assert a single call to auth API.
    assertEquals(2, mAuthRequestCount);
  }

  @Test public void authWithStorageFailure() throws Exception {
    mMockInternalStorage.setShouldFail(true);
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertAuthFailed();
  }

  @Test public void authWithNoPermission() throws Exception {
    // Revoke phone permission.
    mMockPermissionsModule.forbid(Permission.PHONE);
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // Should navigate to ask for permission activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.askForPermissionActivity)),
            TimeUnit.SECONDS.toMillis(1)));
    // Reset permission, to mock the scenario where the user allowed the permission.
    mMockPermissionsModule.reset(Permission.PHONE);
    // Ask for permission again.
    onView(withId(R.id.askPermissionButton)).check(matches(isDisplayed())).perform(click());
    // Should return to Test activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.testActivity)), TimeUnit.SECONDS.toMillis(1)));
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertAuthOk();
    // Assert there was only a single call to auth API, at the second auth attempt.
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
        return new MockResponse().setBody(NetworkUtil.GSON.toJson(mRespondedUser) + "\n");
      }
    });
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // User should be immediately available, as authentication is executed synchronously.
    assertEquals(mRespondedUser.getId(), App.getAuthModule().getUser().getId());
  }

  @Test public void dontAuthWhenUserAlreadyAuthenticated() throws Exception {
    mRespondedUser.save(mActivityTestRule.getActivity());
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    assertAuthOk();
    // Assert there wasn't any auth request.
    assertEquals(0, mAuthRequestCount);
  }

  @Test public void sendToOnBoarding() throws Exception {
    // Undo initialization.
    App.getInternalStorage().delete(mActivityTestRule.getActivity(), User.LAST_USER_PATH);
    // Authenticate user
    App.getAuthModule().auth(mActivityTestRule.getActivity());
    // Should navigate to OnBoarding activity.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.onBoardingActivity)),
        TimeUnit.SECONDS.toMillis(1)));
    // Assert there wasn't any auth request.
    assertEquals(0, mAuthRequestCount);
  }

  private void assertAuthOk() {
    // Should navigate back to Test activity.
    onView(isRoot()).perform(
        waitMatcher(allOf(isDisplayed(), withId(R.id.testActivity)), TimeUnit.SECONDS.toMillis(1)));
    // Assert the user was is saved onto internal storage.
    assertTrue(
        App.getInternalStorage().exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH));
    // Assert the current user now has an ID.
    assertEquals(mRespondedUser.getId(), App.getAuthModule().getUser().getId());
    // Assert the Auth OK status is correct.
    assertTrue(App.getAuthModule().isAuthOk());
  }

  private void assertAuthFailed() {
    // Should navigate to Welcome activity.
    onView(isRoot()).perform(waitMatcher(allOf(isDisplayed(), withId(R.id.welcomeActivity)),
        TimeUnit.SECONDS.toMillis(1)));
    // Should display the error text
    onView(withId(R.id.errorText)).check(matches(isDisplayed()));
    // Assert the current user has no ID or is null.
    if (App.getAuthModule().getUser() != null) {
      assertFalse(App.getAuthModule().getUser().hasId());
    }
    // Assert the Auth OK status is correct.
    assertFalse(App.getAuthModule().isAuthOk());
  }
}