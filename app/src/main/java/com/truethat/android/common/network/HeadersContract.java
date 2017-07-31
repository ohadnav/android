package com.truethat.android.common.network;

/**
 * Proudly created by ohad on 07/06/2017 for TrueThat.
 * <p>
 * Headers name for HTTP communication with out beloved backend.
 */
public enum HeadersContract {
  VERSION_NAME("version-name");

  private static final String PREFIX = "x-truethat-";
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
