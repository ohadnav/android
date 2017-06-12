package com.truethat.android.common.network;

import com.google.gson.GsonBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 12/06/2017 for TrueThat.
 */
public class NetworkUtilTest {
  private static final int NUM = 5;

  @Test public void gsonSerialize() throws Exception {
    String actual = NetworkUtil.GSON.toJson(new MyAndroidClass(NUM));
    String expected = new GsonBuilder().create().toJson(new MyClass(NUM));
    assertEquals(expected, actual);
  }

  @Test public void gsonDeserialize() throws Exception {
    String source = new GsonBuilder().create().toJson(new MyClass(NUM));
    MyAndroidClass expected = new MyAndroidClass(NUM);
    MyAndroidClass actual = NetworkUtil.GSON.fromJson(source, MyAndroidClass.class);
    assertEquals(expected, actual);
  }

  private class MyAndroidClass {
    private int mAwesomeness;

    MyAndroidClass(int anAwesomeness) {
      mAwesomeness = anAwesomeness;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyAndroidClass that = (MyAndroidClass) o;

      return mAwesomeness == that.mAwesomeness;
    }

    @Override public int hashCode() {
      return mAwesomeness;
    }
  }

  private class MyClass {
    private int awesomeness;

    MyClass(int anInt) {
      awesomeness = anInt;
    }

    int getAwesomeness() {
      return awesomeness;
    }
  }
}