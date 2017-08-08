package com.truethat.android.application.storage.internal;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * Proudly created by ohad on 26/05/2017 for TrueThat.
 */

@SuppressWarnings("unchecked") public class DeviceInternalStorageManager
    implements InternalStorageManager {
  private Context mContext;

  public DeviceInternalStorageManager(Context context) {
    mContext = context;
  }

  @Override public void write(String fileName, Serializable data)
      throws IOException {
    String currentSubPath = "";
    for (String subDirectoryPath : fileName.split("/")) {
      currentSubPath += currentSubPath.isEmpty() ? subDirectoryPath : "/" + subDirectoryPath;
      File subDirectory = new File(mContext.getFilesDir() + "/" + currentSubPath);
      if (!subDirectory.exists()) {
        boolean shouldMakeDirectory = !Objects.equals(currentSubPath, fileName);
        if (shouldMakeDirectory && !subDirectory.mkdir()) {
          throw new IOException("Failed to create "
              + currentSubPath
              + " directory in internal storage,"
              + "in order to write into "
              + fileName);
        }
      }
    }
    OutputStream file = new FileOutputStream(mContext.getFilesDir() + "/" + fileName);
    ObjectOutput output = new ObjectOutputStream(file);
    output.writeObject(data);
    file.close();
    output.close();
  }

  @Override public <T extends Serializable> T read(String fileName)
      throws IOException, ClassNotFoundException {
    InputStream file = new FileInputStream(mContext.getFilesDir() + "/" + fileName);
    ObjectInput input = new ObjectInputStream(file);
    T result = (T) input.readObject();
    file.close();
    input.close();
    return result;
  }

  @Override public boolean exists(String fileName) {
    return new File(mContext.getFilesDir() + "/" + fileName).exists();
  }

  @Override public void delete(String fileName) throws IOException {
    File fileOrDirectory = new File(mContext.getFilesDir() + "/" + fileName);
    if (fileOrDirectory.isDirectory()) {
      for (File child : fileOrDirectory.listFiles()) {
        String relativePath = child.getPath();
        if (relativePath.startsWith(mContext.getFilesDir().getPath())) {
          relativePath = relativePath.substring(mContext.getFilesDir().getPath().length());
        }
        delete(relativePath);
      }
    }
    if (!fileOrDirectory.delete()) {
      throw new IOException("File " + fileName + " deletion from internal storage have failed.");
    }
  }
}
