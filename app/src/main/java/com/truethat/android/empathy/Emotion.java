package com.truethat.android.empathy;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.truethat.android.R;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public enum Emotion {
  HAPPY(R.drawable.emoji_happy, 1), SAD(R.drawable.emoji_sad, 2);

  @Expose(serialize = false, deserialize = false) private int mDrawableResource;
  /**
   * Numeric code, to sync with backend.
   */
  @SuppressWarnings("FieldCanBeLocal") @SerializedName("code") private int mCode;

  Emotion(int drawableResource, int code) {
    mDrawableResource = drawableResource;
    mCode = code;
  }

  public int getDrawableResource() {
    return mDrawableResource;
  }
}
