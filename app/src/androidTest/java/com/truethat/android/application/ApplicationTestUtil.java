package com.truethat.android.application;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
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
import com.truethat.android.common.BaseApplicationTestSuite;
import com.truethat.android.common.util.AppUtil;
import java.util.concurrent.TimeoutException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.intent.Checks.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static com.truethat.android.common.BaseApplicationTestSuite.DEFAULT_TIMEOUT;
import static org.hamcrest.CoreMatchers.allOf;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class ApplicationTestUtil {
  public static final String APPLICATION_PACKAGE_NAME = "com.truethat.android.debug";
  public static final String INSTALLER_PACKAGE_NAME = "com.android.packageinstaller";

  /**
   * @param activityClass of {@link AppCompatActivity} to wait for to be displayed.
   * @return action that throws if {@code activityClass} is not displayed within {@link
   * BaseApplicationTestSuite#DEFAULT_TIMEOUT}.
   */
  public static ViewAction waitForActivity(final Class<? extends AppCompatActivity> activityClass) {
    return waitMatcher(allOf(isDisplayed(), withinActivity(activityClass)));
  }

  /**
   * @param viewMatcher for find a specific view.
   * @return action that attempts to find the {@link View} described by {@code viewMatcher} for at
   * most {@link BaseApplicationTestSuite#DEFAULT_TIMEOUT}.
   */
  public static ViewAction waitMatcher(final Matcher viewMatcher) {
    return new ViewAction() {
      @Override public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override public String getDescription() {
        return "waited for to for a specific view with matcher <"
            + viewMatcher
            + "> during "
            + DEFAULT_TIMEOUT
            + ".";
      }

      @Override public void perform(final UiController uiController, final View view) {
        uiController.loopMainThreadUntilIdle();
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + DEFAULT_TIMEOUT.getValueInMS();

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
    };
  }

  /**
   * @param activityClass of activity that should contain the view.
   * @return a matcher that checks whether a view is contained within that activity.
   */
  private static Matcher<View> withinActivity(
      final Class<? extends AppCompatActivity> activityClass) {
    return new TypeSafeMatcher<View>() {
      @Override public void describeTo(Description description) {
        description.appendText("is within " + activityClass.getSimpleName());
      }

      @Override public boolean matchesSafely(View view) {
        return activityClass.isInstance(view.getContext());
      }
    };
  }

  /**
   * @return current foreground activity.
   */
  public static AppCompatActivity getCurrentActivity() {
    getInstrumentation().waitForIdleSync();
    final Activity[] activity = new Activity[1];
    getInstrumentation().runOnMainSync(new Runnable() {
      @Override public void run() {
        java.util.Collection<Activity> activities =
            ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        activity[0] = Iterables.getOnlyElement(activities);
      }
    });
    return (AppCompatActivity) activity[0];
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
        Size windowSize = AppUtil.availableDisplaySize(view);
        return isDisplayed().matches(view)
            && windowSize.getWidth() <= view.getWidth()
            && windowSize.getHeight() <= view.getHeight();
      }
    };
  }

  /**
   * @param color of background
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
}