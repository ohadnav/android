package com.truethat.android.application.permissions;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import com.truethat.android.R;
import java.util.Objects;

/**
 * Proudly created by ohad on 24/05/2017 for TrueThat.
 *
 * Android permissions.
 */

// Next available request code - 3
public enum Permission {
  CAMERA(Manifest.permission.CAMERA, 1,
      R.layout.fragment_no_camera_permission), // For phone number and device ID.
  PHONE(Manifest.permission.READ_PHONE_STATE, 2, R.layout.fragment_no_phone_state_permission);

  /**
   * An Android internal string to describe the permission. Taken from {@link Manifest.permission}.
   */
  private String mManifest;
  /**
   * Permission asking request code, so that the app can distinguish between permission request
   * calls.
   */
  private int mRequestCode;

  /**
   * Resource ID of rationale fragment.
   */
  private int mRationaleFragment;

  Permission(String manifest, int requestCode, int rationaleFragment) {
    mManifest = manifest;
    mRequestCode = requestCode;
    mRationaleFragment = rationaleFragment;
  }

  /**
   * @param manifest Taken from {@link Manifest.permission}, usually returned by {@link
   * AppCompatActivity#onRequestPermissionsResult(int, String[], int[])}.
   * @return the matching {@link Permission} enum.
   */
  public static Permission fromManifest(String manifest) {
    for (Permission permission : Permission.values()) {
      if (Objects.equals(permission.getManifest(), manifest)) return permission;
    }
    throw new IllegalArgumentException(manifest + " is an unrecognized permission.");
  }

  public String getManifest() {
    return mManifest;
  }

  public int getRequestCode() {
    return mRequestCode;
  }

  public int getRationaleFragment() {
    return mRationaleFragment;
  }}
