package com.truethat.android.application.storage.internal;

import java.io.IOException;
import java.io.Serializable;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 * <p>
 * Interface to interact to with the application's internal storage.
 */
public interface InternalStorageManager {
  /**
   * Writes serialized data onto fileName,
   *
   * @param fileName relative path, in relation to mContext.getFilesDir, to the designated file.
   * @param data     to write to the file.
   */
  void write(String fileName, Serializable data) throws IOException;

  /**
   * Reads the contents of fileName, deserializes it, and casts it to type T
   *
   * @param <T>      of the deserialized object.
   * @param fileName relative path, in relation to mContext.getFilesDir, to the designated file.
   *
   * @return the deserialized content of fileName.
   */
  <T extends Serializable> T read(String fileName) throws IOException, ClassNotFoundException;

  /**
   * Deletes fileName from the internal storage.
   *
   * @param fileName to delete.
   */
  void delete(String fileName) throws IOException;
}
