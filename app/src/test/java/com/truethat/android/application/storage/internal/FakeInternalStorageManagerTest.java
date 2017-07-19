package com.truethat.android.application.storage.internal;

import java.io.IOException;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */
public class FakeInternalStorageManagerTest {
  private static final String FILE_NAME = "Lion-King-3-script.txt";
  private FakeInternalStorageManager mFakeInternalStorage;

  @Before public void setUp() throws Exception {
    mFakeInternalStorage = new FakeInternalStorageManager();
  }

  @Test public void writeReadAndDelete() throws Exception {
    Date expected = new Date();
    // Writes to file
    mFakeInternalStorage.write(FILE_NAME, expected);
    // Asserts the file exists.
    assertTrue(mFakeInternalStorage.exists(FILE_NAME));
    // Reads file
    Date actual = mFakeInternalStorage.read(FILE_NAME);
    assertEquals(expected, actual);
    // Deletes file
    mFakeInternalStorage.delete(FILE_NAME);
    // Asserts the file was deleted.
    assertFalse(mFakeInternalStorage.exists(FILE_NAME));
  }

  @Test(expected = IOException.class) public void readShouldFail() throws Exception {
    mFakeInternalStorage.write(FILE_NAME, "");
    mFakeInternalStorage.setShouldFail(true);
    mFakeInternalStorage.read(FILE_NAME);
    mFakeInternalStorage.setShouldFail(false);
    assertTrue(mFakeInternalStorage.exists(FILE_NAME));
  }

  @Test(expected = IOException.class) public void writeShouldFail() throws Exception {
    mFakeInternalStorage.setShouldFail(true);
    mFakeInternalStorage.write(FILE_NAME, "");
  }

  @Test(expected = IOException.class) public void deleteShouldFail() throws Exception {
    mFakeInternalStorage.write(FILE_NAME, "");
    mFakeInternalStorage.setShouldFail(true);
    mFakeInternalStorage.delete(FILE_NAME);
    mFakeInternalStorage.setShouldFail(false);
    assertTrue(mFakeInternalStorage.exists(FILE_NAME));
  }
}