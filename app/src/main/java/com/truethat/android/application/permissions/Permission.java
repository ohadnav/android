package com.truethat.android.application.permissions;

import android.Manifest;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 */

// Next available - 200
public enum Permission {
  CAMERA(Manifest.permission.CAMERA, 100), READ_CONTACTS(Manifest.permission.READ_CONTACTS, 200);

  private String mManifest;
  private int mRequestCode;

  Permission(String manifest, int requestCode) {
    mManifest = manifest;
    mRequestCode = requestCode;
  }

  public String getManifest() {
    return mManifest;
  }

  public int getRequestCode() {
    return mRequestCode;
  }
}
