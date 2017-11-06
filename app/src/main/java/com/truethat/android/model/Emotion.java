package com.truethat.android.model;

import android.support.annotation.DrawableRes;
import com.truethat.android.R;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 *
 * @backend <a>https://github.com/true-that/backend/blob/master/src/main/java/com/truethat/backend/model/Emotion.java</a>
 */

public enum Emotion {
  HAPPY(R.drawable.emoji_happy), FEAR(R.drawable.emoji_omg), // Deprecated
  SURPRISE(R.drawable.emoji_omg), // Deprecated
  OMG(R.drawable.emoji_omg), DISGUST(R.drawable.emoji_disgust);

  public static Emotion[] SUPPORTED_VALUES = { HAPPY, OMG, DISGUST };

  private int mDrawableResource;

  Emotion(int drawableResource) {
    mDrawableResource = drawableResource;
  }

  @DrawableRes public int getDrawableResource() {
    return mDrawableResource;
  }
}
