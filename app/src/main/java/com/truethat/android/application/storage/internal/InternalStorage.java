package com.truethat.android.application.storage.internal;

import android.content.Context;
import java.io.IOException;
import java.io.Serializable;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 * <p>
 * Interface to interact to with the application's internal storage.
 */
public interface InternalStorage {
  /**
   * Writes serialized data onto fileName,
   *
   * @param context for which to write internal storage.
   * @param fileName relative path, in relation to context.getFilesDir, to the designated file.
   * @param data to write to the file.
   */
  void write(Context context, String fileName, Serializable data) throws IOException;

  /**
   * Reads the contents of fileName, deserializes it, and casts it to type T
   *
   * @param context for which to read from internal storage.
   * @param fileName relative path, in relation to context.getFilesDir, to the designated file.
   * @param <T> of the deserialized object.
   * @return the deserialized content of fileName.
   */
  <T extends Serializable> T read(Context context, String fileName) throws IOException, ClassNotFoundException;

  /**
   * Asses whether fileName exists.
   *
   * @param context for which to read from internal storage.
   * @param fileName relative path, in relation to context.getFilesDir, to the designated file.
   * @return the deserialized content of fileName.
   */
  boolean exists(Context context, String fileName);

  /**
   * Deletes fileName from the internal storage.
   *
   * @param context for which to delete from internal storage.
   * @param fileName to delete.
   */
  void delete(Context context, String fileName) throws IOException;
}
