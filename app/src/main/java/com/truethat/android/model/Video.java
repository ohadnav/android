package com.truethat.android.model;

import android.support.annotation.Nullable;
import java.io.Serializable;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 */

public class Video extends Media implements Serializable {
  private static final long serialVersionUID = 3860634597513193683L;
  /**
   * Internal path to video file on local storage.
   */
  private transient String mInternalPath;

  Video(@Nullable String url, @Nullable String internalPath) {
    super(url);
    mInternalPath = internalPath;
  }

  public String getInternalPath() {
    return mInternalPath;
  }
}
