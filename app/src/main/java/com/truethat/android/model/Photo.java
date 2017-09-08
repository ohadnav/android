package com.truethat.android.model;

import android.support.annotation.Nullable;
import java.io.Serializable;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 */

public class Photo extends Media implements Serializable {
  private static final long serialVersionUID = 7108700087565678875L;
  private byte[] mBytes;

  Photo(@Nullable String url, @Nullable byte[] bytes) {
    super(url);
    mBytes = bytes;
  }

  public byte[] getBytes() {
    return mBytes;
  }
}
