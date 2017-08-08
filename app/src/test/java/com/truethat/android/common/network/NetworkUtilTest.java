package com.truethat.android.common.network;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Reactable;
import com.truethat.android.model.Scene;
import com.truethat.android.model.User;
import java.util.Date;
import java.util.TreeMap;
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
  private static final TreeMap<Emotion, Long> EMOTIONAL_REACTIONS = new TreeMap<Emotion, Long>() {{
    put(Emotion.HAPPY, 10L);
  }};
  private static final Reactable SCENE =
      new Scene(1, "url", new User("elon", "musk", null, null), EMOTIONAL_REACTIONS, DATE,
          Emotion.HAPPY);

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

  @Test public void gsonSerialize_reactable() throws Exception {
    JsonElement serialized = GSON.toJsonTree(SCENE);
    // Should have type.
    assertEquals(SCENE.getClass().getSimpleName(),
        serialized.getAsJsonObject().get(TYPE_FIELD_NAME).getAsString());
  }

  @Test public void gsonDeserialize_reactable() throws Exception {
    Scene actual = (Scene) GSON.fromJson(GSON.toJson(SCENE), Reactable.class);
    assertEquals(SCENE, actual);
  }

  private class MyAndroidClass {
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