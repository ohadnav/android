package com.truethat.android.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Proudly created by ohad on 16/10/2017 for TrueThat.
 * <p>
 * Big thanks to https://stackoverflow.com/a/5837739/4349707
 */

public class SerializableTestUtil {

  private static byte[] serialize(Object o) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
        objectOutputStream.writeObject(o);
      }
      return outputStream.toByteArray();
    }
  }

  private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
      try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
        return objectInputStream.readObject();
      }
    }
  }

  public static void testSerializability(Object obj) throws Exception {
    assertEquals(obj, deserialize(serialize(obj)));
  }
}
