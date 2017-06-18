package com.truethat.android.auth;

import com.truethat.android.application.App;
import com.truethat.android.common.BaseApplicationTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 16/06/2017 for TrueThat.
 */
public class UserTest extends BaseApplicationTest {

  @Test public void isValidName() throws Exception {
    List<String> validNames = Arrays.asList("ohad navon", "oHAd Navon", "oh ad");
    List<String> invalidNames = Arrays.asList("ohadnavon", "o n", "oh a", "ohad 2navon");
    for (String invalidName : invalidNames) {
      assertFalse(User.isValidName(invalidName));
    }
    for (String validName : validNames) {
      assertTrue(User.isValidName(validName));
    }
  }

  @Test public void extractFirstName() throws Exception {
    assertEquals("ohad", User.extractFirstName("ohad navon"));
    assertEquals("ohad", User.extractFirstName("oHAd navon"));
    assertEquals("ohad", User.extractFirstName("Ohad navon"));
    assertEquals("ohad", User.extractFirstName("ohad navon the third"));
  }

  @Test public void extractLastName() throws Exception {
    assertEquals("", User.extractLastName("ohad"));
    assertEquals("navon", User.extractLastName("ohad navon"));
    assertEquals("navon", User.extractLastName("ohad Navon"));
    assertEquals("navon", User.extractLastName("ohad nAVon"));
    assertEquals("navon the third", User.extractLastName("ohad navon the third"));
  }

  @Test public void updateNames() throws Exception {
    @SuppressWarnings("ConstantConditions") User user = new User(null, null, null, null, null);
    user.updateNames("speedy gonzales", mActivityTestRule.getActivity());
    assertEquals("speedy gonzales", user.getDisplayName());
    // Double spaces are shrunk.
    user.updateNames("ab  cd", mActivityTestRule.getActivity());
    assertEquals("ab cd", user.getDisplayName());
    // Names are trimmed
    user.updateNames("  speedy gonzales  ", mActivityTestRule.getActivity());
    assertEquals("speedy gonzales", user.getDisplayName());
    // Last name can have multiple words in it.
    user.updateNames("speedy gonzales the second", mActivityTestRule.getActivity());
    assertEquals("speedy gonzales the second", user.getDisplayName());
  }

  @Test public void updateNamesDontSaveWhenSignedOut() throws Exception {
    User user = new User(null, null, null, null, null);
    user.updateNames("speedy gonzales", mActivityTestRule.getActivity());
    assertEquals("speedy gonzales", user.getDisplayName());
    // Assert not saved.
    assertFalse(
        App.getInternalStorage().exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH));
  }

  @Test public void updateNamesSaves() throws Exception {
    User user = new User(0L, null, null, null, null);
    user.updateNames("speedy gonzales", mActivityTestRule.getActivity());
    assertEquals("speedy gonzales", user.getDisplayName());
    // Assert not saved.
    assertTrue(
        App.getInternalStorage().exists(mActivityTestRule.getActivity(), User.LAST_USER_PATH));
  }
}