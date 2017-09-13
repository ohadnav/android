package com.truethat.android.model;

import android.support.annotation.DrawableRes;
import com.truethat.android.R;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 *
 * @backend <a>https://goo.gl/dWSkXd</a>
 */

public enum Emotion {
  HAPPY(R.drawable.emoji_happy), FEAR(R.drawable.emoji_fear), SURPRISE(
      R.drawable.emoji_surprise), DISGUST(R.drawable.emoji_disgust);

  private int mDrawableResource;

  Emotion(int drawableResource) {
    mDrawableResource = drawableResource;
  }

  @DrawableRes public int getDrawableResource() {
    return mDrawableResource;
  }
}
