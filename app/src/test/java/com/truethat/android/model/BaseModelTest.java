package com.truethat.android.model;

import org.junit.Test;

import static com.truethat.android.common.util.SerializableTestUtil.testSerializability;

/**
 * Proudly created by ohad on 16/10/2017 for TrueThat.
 */
public class BaseModelTest {
  @Test public void testSerializable() throws Exception {
    testSerializability(new BaseModel(10L));
    testSerializability(new BaseModel());
  }
}