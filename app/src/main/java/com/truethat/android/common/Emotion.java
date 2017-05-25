package com.truethat.android.common;

import com.truethat.android.R;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public enum Emotion {
    HAPPY,
    SAD;

    public static int drawableResource(Emotion emotion) {
        switch (emotion) {
            case HAPPY:
                return R.drawable.emoji_happy;
            case SAD:
                return R.drawable.emoji_sad;
            default:
                throw new IllegalArgumentException("Emotion has no drawable resource.");
        }
    }
}
