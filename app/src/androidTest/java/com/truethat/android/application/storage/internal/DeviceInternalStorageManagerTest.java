package com.truethat.android.application.storage.internal;

import com.truethat.android.common.BaseApplicationTestSuite;
import java.io.File;
import java.util.Date;
import org.junit.Test;

import static com.truethat.android.application.ApplicationTestUtil.getApp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */
public class DeviceInternalStorageManagerTest extends BaseApplicationTestSuite {
  private static final String FILE_NAME =
      "DefaultInternalStorageTest_" + new Date().getTime() + ".txt";
  private static final String ROOT_DIR =
      "DefaultInternalStorageTest_nested_" + new Date().getTime();
  private static final String NESTED_FILE = ROOT_DIR + "/asta/la/vista.baby";
  private InternalStorageManager mInternalStorage = new DeviceInternalStorageManager(getApp());

  @Test public void writeReadAndDelete() throws Exception {
    Date expected = new Date();
    // Writes to file
    mInternalStorage.write(FILE_NAME, expected);
    // Reads file
    Date actual = mInternalStorage.read(FILE_NAME);
    assertEquals(expected, actual);
    // Deletes file
    mInternalStorage.delete(FILE_NAME);
    assertFalse(new File(mActivityTestRule.getActivity().getFilesDir() + "/" + FILE_NAME).exists());
  }

  @Test public void writeReadAndDelete_nestedFile() throws Exception {
    String expected = "The Terminator";
    // Writes to file
    mInternalStorage.write(NESTED_FILE, expected);
    // Reads file
    String actual = mInternalStorage.read(NESTED_FILE);
    assertEquals(expected, actual);
    // Deletes file
    mInternalStorage.delete(NESTED_FILE);
    assertFalse(
        new File(mActivityTestRule.getActivity().getFilesDir() + "/" + NESTED_FILE).exists());
    // Delete root dir.
    mInternalStorage.delete(ROOT_DIR);
    assertFalse(new File(mActivityTestRule.getActivity().getFilesDir() + "/" + ROOT_DIR).exists());
  }
}