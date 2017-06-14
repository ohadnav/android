package com.truethat.android.application.storage.internal;

import android.support.test.rule.ActivityTestRule;
import com.truethat.android.common.util.TestActivity;
import java.io.File;
import java.util.Date;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */
public class DefaultInternalStorageTest {
  private static final String FILE_NAME =
      "DefaultInternalStorageTest_" + new Date().getTime() + ".txt";
  private static final String ROOT_DIR =
      "DefaultInternalStorageTest_nested_" + new Date().getTime();
  private static final String NESTED_FILE = ROOT_DIR + "/asta/la/vista.baby";

  @Rule public ActivityTestRule<TestActivity> mActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, true);
  private InternalStorage mInternalStorage = new DefaultInternalStorage();

  @Test public void writeReadAndDelete() throws Exception {
    Date expected = new Date();
    // Writes to file
    mInternalStorage.write(mActivityTestRule.getActivity(), FILE_NAME, expected);
    // Reads file
    Date actual = mInternalStorage.read(mActivityTestRule.getActivity(), FILE_NAME);
    assertEquals(expected, actual);
    // Deletes file
    mInternalStorage.delete(mActivityTestRule.getActivity(), FILE_NAME);
    assertFalse(new File(mActivityTestRule.getActivity().getFilesDir() + "/" + FILE_NAME).exists());
  }

  @Test public void writeReadAndDelete_nestedFile() throws Exception {
    String expected = "The Terminator";
    // Writes to file
    mInternalStorage.write(mActivityTestRule.getActivity(), NESTED_FILE, expected);
    // Reads file
    String actual = mInternalStorage.read(mActivityTestRule.getActivity(), NESTED_FILE);
    assertEquals(expected, actual);
    // Deletes file
    mInternalStorage.delete(mActivityTestRule.getActivity(), NESTED_FILE);
    assertFalse(
        new File(mActivityTestRule.getActivity().getFilesDir() + "/" + NESTED_FILE).exists());
    // Delete root dir.
    mInternalStorage.delete(mActivityTestRule.getActivity(), ROOT_DIR);
    assertFalse(new File(mActivityTestRule.getActivity().getFilesDir() + "/" + ROOT_DIR).exists());
  }
}