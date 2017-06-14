package com.truethat.android.common.util;

import android.support.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 25/05/2017 for TrueThat.
 */
public class AssetsReaderUtilTest {
  private static final String TINY_FILE_PATH = "common/util/tiny_file.txt";
  @Rule public ActivityTestRule<TestActivity> mTestActivityTestRule =
      new ActivityTestRule<>(TestActivity.class, true, true);

  @Test public void readAsBytes() throws Exception {
    byte[] res = AssetsReaderUtil.readAsBytes(mTestActivityTestRule.getActivity(), TINY_FILE_PATH);
    assertEquals("My name is Inigo Montoya", new String(res, "UTF-8"));
  }
}