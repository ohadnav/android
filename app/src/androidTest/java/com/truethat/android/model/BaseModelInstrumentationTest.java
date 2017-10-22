package com.truethat.android.model;

import org.junit.Test;

import static com.truethat.android.common.util.ParcelableTestUtil.testParcelability;

/**
 * Proudly created by ohad on 16/10/2017 for TrueThat.
 */
public class BaseModelInstrumentationTest {
  @Test public void testParcelable() throws Exception {
    testParcelability(new BaseModel(10L), BaseModel.CREATOR);
    testParcelability(new BaseModel(), BaseModel.CREATOR);
  }
}