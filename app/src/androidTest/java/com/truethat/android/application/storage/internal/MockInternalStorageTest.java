package com.truethat.android.application.storage.internal;

import com.truethat.android.common.BaseApplicationTestSuite;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */
public class MockInternalStorageTest extends BaseApplicationTestSuite {
  private static final String FILE_NAME = "Lion-King-3-script.txt";

  @Test public void writeReadAndDelete() throws Exception {
    Date expected = new Date();
    // Writes to file
    mMockInternalStorage.write(mActivityTestRule.getActivity(), FILE_NAME, expected);
    // Asserts the file exists.
    assertTrue(mMockInternalStorage.exists(mActivityTestRule.getActivity(), FILE_NAME));
    // Reads file
    Date actual = mMockInternalStorage.read(mActivityTestRule.getActivity(), FILE_NAME);
    assertEquals(expected, actual);
    // Deletes file
    mMockInternalStorage.delete(mActivityTestRule.getActivity(), FILE_NAME);
    // Asserts the file was deleted.
    assertFalse(mMockInternalStorage.exists(mActivityTestRule.getActivity(), FILE_NAME));
  }

  @Test(expected = IOException.class) public void readShouldFail() throws Exception {
    mMockInternalStorage.setShouldFail(true);
    mMockInternalStorage.write(mActivityTestRule.getActivity(), FILE_NAME, "");
    mMockInternalStorage.read(mActivityTestRule.getActivity(), FILE_NAME);
  }

  @Test(expected = IOException.class) public void writeShouldFail() throws Exception {
    mMockInternalStorage.setShouldFail(true);
    mMockInternalStorage.write(mActivityTestRule.getActivity(), FILE_NAME, "");
  }

  @Test(expected = IOException.class) public void deleteShouldFail() throws Exception {
    mMockInternalStorage.setShouldFail(true);
    mMockInternalStorage.write(mActivityTestRule.getActivity(), FILE_NAME, "");
    mMockInternalStorage.delete(mActivityTestRule.getActivity(), FILE_NAME);
  }
}