package com.truethat.android.empathy;

import com.google.gson.annotations.Expose;
import com.truethat.android.R;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public enum Emotion {
  HAPPY(R.drawable.emoji_happy), SAD(R.drawable.emoji_sad);

  @Expose(serialize = false, deserialize = false) private int mDrawableResource;

  Emotion(int drawableResource) {
    mDrawableResource = drawableResource;
  }

  public int getDrawableResource() {
    return mDrawableResource;
  }
}
