package com.truethat.android.model;

import org.junit.Test;

import static com.truethat.android.common.util.ParcelableTestUtil.testParcelability;

/**
 * Proudly created by ohad on 16/10/2017 for TrueThat.
 */
public class PhotoInstrumentationTest {
  @Test public void testParcelable() throws Exception {
    testParcelability(new Photo(10L, "a"), Photo.CREATOR);
  }
}