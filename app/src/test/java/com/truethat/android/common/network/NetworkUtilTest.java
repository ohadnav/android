package com.truethat.android.common.network;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.truethat.android.model.Media;
import com.truethat.android.model.Photo;
import java.util.Date;
import org.junit.Test;

import static com.truethat.android.common.network.NetworkUtil.GSON;
import static com.truethat.android.external.RuntimeTypeAdapterFactory.TYPE_FIELD_NAME;
import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 08/08/2017 for TrueThat.
 */
public class NetworkUtilTest {
  private static final int NUM = 5;
  private static final Date DATE = new Date(0);
  private static final String UTC_DATE = "\"1970-01-01T00:00:00.000+0000\"";
  private static final Media photo = new Photo(1L, "example.com");

  @Test public void gsonSerialize_namingStrategy() throws Exception {
    String actual = GSON.toJson(new MyAndroidClass(NUM));
    String expected = new GsonBuilder().create().toJson(new MyClass(NUM));
    assertEquals(expected, actual);
  }

  @Test public void gsonDeserialize_namingStrategy() throws Exception {
    String source = new GsonBuilder().create().toJson(new MyClass(NUM));
    MyAndroidClass expected = new MyAndroidClass(NUM);
    MyAndroidClass actual = GSON.fromJson(source, MyAndroidClass.class);
    assertEquals(expected, actual);
  }

  @Test public void gsonSerialize_date() throws Exception {
    String actual = GSON.toJson(DATE);
    assertEquals(UTC_DATE, actual);
  }

  @Test public void gsonDeserialize_date() throws Exception {
    Date actual = GSON.fromJson(UTC_DATE, Date.class);
    assertEquals(DATE, actual);
  }

  @Test public void gsonSerialize_subtype() throws Exception {
    JsonElement serialized = GSON.toJsonTree(photo);
    // Should have type.
    assertEquals(photo.getClass().getSimpleName(),
        serialized.getAsJsonObject().get(TYPE_FIELD_NAME).getAsString());
  }

  @Test public void gsonDeserialize_subtype() throws Exception {
    Photo actual = (Photo) GSON.fromJson(GSON.toJson(photo), Media.class);
    assertEquals(photo, actual);
  }

  @SuppressWarnings("SameParameterValue") private class MyAndroidClass {
    private int mAwesomeness;

    MyAndroidClass(int anAwesomeness) {
      mAwesomeness = anAwesomeness;
    }

    @Override public int hashCode() {
      return mAwesomeness;
    }

    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MyAndroidClass that = (MyAndroidClass) o;

      return mAwesomeness == that.mAwesomeness;
    }
  }

  @SuppressWarnings({ "SameParameterValue", "unused" }) private class MyClass {
    private int awesomeness;

    MyClass(int anInt) {
      awesomeness = anInt;
    }

    int getAwesomeness() {
      return awesomeness;
    }
  }
}