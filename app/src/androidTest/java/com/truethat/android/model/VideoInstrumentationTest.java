package com.truethat.android.model;

import org.junit.Test;

import static com.truethat.android.common.util.ParcelableTestUtil.testParcelability;

/**
 * Proudly created by ohad on 16/10/2017 for TrueThat.
 */
public class VideoInstrumentationTest {
  @Test public void testParcelable() throws Exception {
    testParcelability(new Video(10L, "a"), Video.CREATOR);
    testParcelability(new Video("a"), Video.CREATOR);
    testParcelability(new Video(1), Video.CREATOR);
  }
}