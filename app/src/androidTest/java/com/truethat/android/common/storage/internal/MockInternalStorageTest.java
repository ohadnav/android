package com.truethat.android.common.storage.internal;

import android.support.test.rule.ActivityTestRule;

import com.truethat.android.common.TestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */
public class MockInternalStorageTest {
    private static final String FILE_NAME = "Lion-King-3-script.txt";

    @Rule
    public ActivityTestRule<TestActivity> mActivityTestRule =
            new ActivityTestRule<>(TestActivity.class, true, false);
    private MockInternalStorage mInternalStorage;

    @Before
    public void setUp() throws Exception {
        // Initialize MockInternalStorage
        mInternalStorage = new MockInternalStorage();
    }

    @Test
    public void writeReadAndDelete() throws Exception {
        Date expected = new Date();
        // Writes to file
        mInternalStorage.write(mActivityTestRule.getActivity(), FILE_NAME, expected);
        // Asserts the file exists.
        assertTrue(mInternalStorage.exists(FILE_NAME));
        // Reads file
        Date actual = mInternalStorage.read(mActivityTestRule.getActivity(), FILE_NAME);
        assertEquals(expected, actual);
        // Deletes file
        mInternalStorage.delete(mActivityTestRule.getActivity(), FILE_NAME);
        // Asserts the file was deleted.
        assertFalse(mInternalStorage.exists(FILE_NAME));
    }
}