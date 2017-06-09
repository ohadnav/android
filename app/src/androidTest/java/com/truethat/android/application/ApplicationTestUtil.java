package com.truethat.android.application;

import android.graphics.Rect;
import android.support.test.espresso.PerformException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.util.HumanReadables;
import android.support.test.espresso.util.TreeIterables;
import android.view.View;
import java.util.concurrent.TimeoutException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */
public class ApplicationTestUtil {
  public static final String APPLICATION_PACKAGE_NAME = "com.truethat.android";
  public static final String INSTALLER_PACKAGE_NAME = "com.android.packageinstaller";

  public static ViewAction waitMatcher(final Matcher viewMatcher, final long millis) {
    return new ViewAction() {
      @Override public Matcher<View> getConstraints() {
        return isRoot();
      }

      @Override public String getDescription() {
        return "waited for to for a specific view with matcher <" + viewMatcher + "> during " + millis + " millis.";
      }

      @Override public void perform(final UiController uiController, final View view) {
        uiController.loopMainThreadUntilIdle();
        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + millis;

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

  public static Matcher<View> isFullScreen() {
    return new TypeSafeMatcher<View>() {
      @Override public void describeTo(Description description) {
        description.appendText("is displayed fullscreen to the user");
      }

      @Override public boolean matchesSafely(View view) {
        Rect windowRect = new Rect();
        view.getWindowVisibleDisplayFrame(windowRect);
        int windowHeight = windowRect.bottom - windowRect.top;
        int windiwWidth = windowRect.right - windowRect.left;
        return isDisplayed().matches(view) && windiwWidth == view.getWidth() && windowHeight == view.getHeight();
      }
    };
  }
}