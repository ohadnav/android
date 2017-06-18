package com.truethat.android.application.storage.internal;

import android.content.Context;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */

public class MockInternalStorage implements InternalStorage {
  /**
   * Objects are mapped to byte arrays to test serialization as well.
   */
  private Map<String, byte[]> mFileNameToBytes = new HashMap<>();
  /**
   * Whether the next operation should throw an error.
   */
  private boolean mShouldFail = false;

  @Override public void write(Context context, String fileName, Serializable data)
      throws IOException {

    if (mShouldFail) fail();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ObjectOutput objectOutput = new ObjectOutputStream(outputStream);
    objectOutput.writeObject(data);
    mFileNameToBytes.put(fileName, outputStream.toByteArray());
    outputStream.close();
    objectOutput.close();
  }

  @Override public <T extends Serializable> T read(Context context, String fileName)
      throws IOException, ClassNotFoundException {
    if (mShouldFail) fail();
    if (!exists(context, fileName)) {
      throw new IOException("File " + fileName + " does not exist.");
    }
    InputStream inputStream = new ByteArrayInputStream(mFileNameToBytes.get(fileName));
    ObjectInput objectInput = new ObjectInputStream(inputStream);
    @SuppressWarnings("unchecked") T result = (T) objectInput.readObject();
    inputStream.close();
    objectInput.close();
    return result;
  }

  @Override public void delete(Context context, String fileName) throws IOException {
    if (mShouldFail) fail();
    if (!exists(context, fileName)) {
      throw new IOException("File " + fileName + " does not exist.");
    }
    mFileNameToBytes.remove(fileName);
  }

  @Override public boolean exists(Context context, String fileName) {
    return mFileNameToBytes.containsKey(fileName);
  }

  public void setShouldFail(boolean shouldFail) {
    mShouldFail = shouldFail;
  }

  private void fail() throws IOException {
    mShouldFail = false;
    throw new IOException("Fake error");
  }
}
