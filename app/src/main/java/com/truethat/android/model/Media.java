package com.truethat.android.model;

import android.support.annotation.Nullable;
import java.io.Serializable;

/**
 * Proudly created by ohad on 07/09/2017 for TrueThat.
 */

public abstract class Media implements Serializable {
  private static final long serialVersionUID = 6966859003865108004L;
  private String mUrl;

  Media(@Nullable String url) {
    mUrl = url;
  }

  public String getUrl() {
    return mUrl;
  }
}
