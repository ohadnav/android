package com.truethat.android.common.util;

import android.support.test.rule.ActivityTestRule;

import com.truethat.android.common.TestActivity;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */
public class AssetsReaderUtilTest {
    private static final String                         TINY_FILE_PATH        =
            "common/util/tiny_file.txt";
    private static final String                         LARGE_FILE_PATH       =
            "common/util/proclamation_of_israel_independence.txt";
    @Rule
    public               ActivityTestRule<TestActivity> mTestActivityTestRule =
            new ActivityTestRule<>(TestActivity.class, true, true);

    @Test
    public void read_tinyFile() throws Exception {
        String res = AssetsReaderUtil.read(mTestActivityTestRule.getActivity(), TINY_FILE_PATH);
        assertEquals("My name is Inigo Montoya", res);
    }

    @Test
    public void read_largeFile() throws Exception {
        String res = AssetsReaderUtil.read(mTestActivityTestRule.getActivity(), LARGE_FILE_PATH);
        assertTrue(res.startsWith("Provisional Government of Israel"));
        assertTrue(res.endsWith("Moshe Shertok"));
    }
}