package com.truethat.android.application;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import com.truethat.android.common.BaseInstrumentationTestSuite;
import com.truethat.android.view.activity.BaseActivity;
import java.util.concurrent.TimeoutException;
import org.awaitility.Duration;
import org.awaitility.core.ThrowingRunnable;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.intent.Checks.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.truethat.android.common.BaseInstrumentationTestSuite.TIMEOUT;
import static com.truethat.android.common.util.AppUtil.availableDisplaySize;
import static com.truethat.android.common.util.AppUtil.realDisplaySize;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class ApplicationTestUtil {
  public static final String APPLICATION_PACKAGE_NAME = "com.truethat.android.debug";
  public static final String INSTALLER_PACKAGE_NAME = "com.android.packageinstaller";

  /**
   * @param activityClass of {@link AppCompatActivity} to wait for to be displayed.
   */
  public static void waitForActivity(final Class<? extends AppCompatActivity> activityClass,
      Duration duration) {
    if (getCurrentActivity() == null) {
      throw new AssertionError("App has not started. Device locked?");
    }
    await().atMost(duration).untilAsserted(new ThrowingRunnable() {
      @Override public void run() throws Throwable {
        assertEquals(activityClass.getSimpleName(),
            getCurrentActivity().getClass().getSimpleName());
      }
    });
  }

  public static void waitForActivity(final Class<? extends AppCompatActivity> activityClass) {
    waitForActivity(activityClass, TIMEOUT);
  }

  /**
   * Attempts to find the {@link View} described by {@code viewMatcher} for at
   * most {@link BaseInstrumentationTestSuite#TIMEOUT}.
   *
   * @param viewMatcher for find a specific view.
   */
  public static void waitMatcher(final Matcher viewMatcher) {
    onView(isRoot()).perform(new ViewAction() {
      @Override public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override public String getDescription() {
        return "waited for to for a specific view with matcher <"
            + viewMatcher
            + "> during "
            + TIMEOUT
            + ".";
      }

      @Override public void perform(final UiController uiController, final View view) {
        uiController.loopMainThreadUntilIdle();
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + TIMEOUT.getValueInMS();

        do {
          for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
            // found view that satisfies viewMatcher
            if (viewMatcher.matches(child)) {
              return;
            }
          }

          uiController.loopMainThreadForAtLeast(50);
        } while (System.currentTimeMillis() < endTime);

        // timeout happens
        throw new PerformException.Builder().withActionDescription(this.getDescription())
            .withViewDescription(HumanReadables.describe(view))
            .withCause(new TimeoutException())
            .build();
      }
    });
  }

  /**
   * @return current foreground activity.
   */
  public static AppCompatActivity getCurrentActivity() {
    try {
      getInstrumentation().waitForIdleSync();
      final BaseActivity[] activity = new BaseActivity[1];
      getInstrumentation().runOnMainSync(new Runnable() {
        @Override public void run() {
          activity[0] = getCurrentActivityIgnoreThread();
        }
      });
      return activity[0];
    } catch (RuntimeException threadException) {
      return getCurrentActivityIgnoreThread();
    }
  }

  private static BaseActivity getCurrentActivityIgnoreThread() {
    java.util.Collection<Activity> activities =
        ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
    try {
      return (BaseActivity) Iterables.getOnlyElement(activities);
    } catch (Exception ignored) {
      return null;
    }
  }

  /**
   * @return Matcher to assert whether a view is displayed full screen.
   */
  public static Matcher<View> isFullScreen() {
    return new TypeSafeMatcher<View>() {
      @Override public void describeTo(Description description) {
        description.appendText("is displayed fullscreen to the user");
      }

      @Override public boolean matchesSafely(View view) {
        Size windowSize = availableDisplaySize(view);
        return isDisplayed().matches(view)
            && windowSize.getWidth() <= view.getWidth()
            && windowSize.getHeight() <= view.getHeight();
      }
    };
  }

  /**
   * @return whether a view is displayed full screen.
   */
  public static boolean isFullscreen(View view) {
    Size windowSize = availableDisplaySize(view);
    return isDisplayed().matches(view)
        && windowSize.getWidth() <= view.getWidth()
        && windowSize.getHeight() <= view.getHeight();
  }

  /**
   * @param color of background
   *
   * @return Matcher to assert whether a {@link EditText} has a certain background color.
   */
  public static Matcher<View> withBackgroundColor(final int color) {
    checkNotNull(color);
    return new BoundedMatcher<View, EditText>(EditText.class) {
      @Override public boolean matchesSafely(EditText matcher) {
        return matcher.getBackgroundTintList() != null && color == matcher.getBackgroundTintList()
            .getDefaultColor();
      }

      @Override public void describeTo(Description description) {
        description.appendText("with background color: ");
      }
    };
  }

  public static boolean isDebugging() {
    return android.os.Debug.isDebuggerConnected() || 0 != (
        getInstrumentation().getContext().getApplicationInfo().flags &=
            ApplicationInfo.FLAG_DEBUGGABLE);
  }

  public static void centerSwipeUp() {
    onView(withId(android.R.id.content)).perform(
        new GeneralSwipeAction(Swipe.FAST, GeneralLocation.CENTER, GeneralLocation.TOP_CENTER,
            Press.FINGER));
  }

  public static boolean isKeyboardVisible() {
    // 0.85 ratio is perhaps enough to determine keypad height.
    return availableDisplaySize(getCurrentActivity().findViewById(android.R.id.content)).getHeight()
        < realDisplaySize(getCurrentActivity()).y * 0.85;
  }

  public static App getApp() {
    return (App) InstrumentationRegistry.getInstrumentation()
        .getTargetContext()
        .getApplicationContext();
  }

  public static int getAbsoluteLeft(View view) {
    if (view.getParent() == view.getRootView()) {
      return view.getLeft();
    } else {
      return view.getLeft() + getAbsoluteLeft((View) view.getParent());
    }
  }

  public static int getAbsoluteTop(View view) {
    if (view.getParent() == view.getRootView()) {
      return view.getTop();
    } else {
      return view.getTop() + getAbsoluteTop((View) view.getParent());
    }
  }
}