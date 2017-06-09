package com.truethat.android.common.network;

/**
 * Proudly created by ohad on 07/06/2017 for TrueThat.
 *
 * Headers name for HTTP communication with out beloved backend.
 */
enum HeadersContract {
  BUILD_NUMBER("build-number");

  public static final String PREFIX = "x-thuethat-";
  private String mName;

  HeadersContract(String name) {
    mName = name;
  }

  public String getName() {
    return PREFIX + mName;
  }

  @Override public String toString() {
    return getName();
  }
}
