package com.truethat.android.common.util;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static com.truethat.android.common.util.StringUtil.toTitleCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Proudly created by ohad on 05/07/2017 for TrueThat.
 */
public class StringUtilTest {
  @Test public void titleCase() throws Exception {
    assertEquals("Matt Damon", toTitleCase("matt damon"));
  }

  @Test public void isValidFullName() throws Exception {
    List<String> validNames = Arrays.asList("ohad navon", "oHAd Navon", "oh ad");
    List<String> invalidNames = Arrays.asList("ohadnavon", "o n", "oh a", "ohad 2navon");
    for (String invalidName : invalidNames) {
      assertFalse(StringUtil.isValidFullName(invalidName));
    }
    for (String validName : validNames) {
      assertTrue(StringUtil.isValidFullName(validName));
    }
  }

  @Test public void extractFirstName() throws Exception {
    assertEquals("ohad", StringUtil.extractFirstName("ohad navon"));
    assertEquals("ohad", StringUtil.extractFirstName("oHAd navon"));
    assertEquals("ohad", StringUtil.extractFirstName("Ohad navon"));
    assertEquals("ohad", StringUtil.extractFirstName("ohad navon the third"));
  }

  @Test public void extractLastName() throws Exception {
    assertEquals("", StringUtil.extractLastName("ohad"));
    assertEquals("navon", StringUtil.extractLastName("ohad navon"));
    assertEquals("navon", StringUtil.extractLastName("ohad Navon"));
    assertEquals("navon", StringUtil.extractLastName("ohad nAVon"));
    assertEquals("navon the third", StringUtil.extractLastName("ohad navon the third"));
  }
}