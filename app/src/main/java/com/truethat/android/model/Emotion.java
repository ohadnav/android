package com.truethat.android.model;

import com.truethat.android.R;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/dWSkXd</a>
 */

public enum Emotion {
  HAPPY(R.drawable.emoji_happy), SAD(R.drawable.emoji_sad);

  private int mDrawableResource;

  Emotion(int drawableResource) {
    mDrawableResource = drawableResource;
  }

  public int getDrawableResource() {
    return mDrawableResource;
  }
}
