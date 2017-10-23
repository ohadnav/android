package com.truethat.android.model;

import android.support.annotation.DrawableRes;
import com.truethat.android.R;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/Emotion.java</a>
 */

public enum Emotion {
  HAPPY(R.drawable.emoji_happy), OMG(R.drawable.emoji_omg), DISGUST(R.drawable.emoji_disgust);

  private int mDrawableResource;

  Emotion(int drawableResource) {
    mDrawableResource = drawableResource;
  }

  @DrawableRes public int getDrawableResource() {
    return mDrawableResource;
  }
}
