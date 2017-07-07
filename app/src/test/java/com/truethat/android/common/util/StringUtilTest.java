package com.truethat.android.common.util;

import org.junit.Test;

import static com.truethat.android.common.util.StringUtil.toTitleCase;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 05/07/2017 for TrueThat.
 */
public class StringUtilTest {
  @Test public void titleCase() throws Exception {
    assertEquals("Matt Damon", toTitleCase("matt damon"));
    assertEquals("Matt  Damon", toTitleCase("matt  damon"));
  }
}