package com.truethat.android.theater;

import android.media.Image;

import com.truethat.android.common.Emotion;

import java.util.HashMap;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public class Scene {
    private String                 mId;
    private Image                  mImage;
    private HashMap<Emotion, Long> mReactionCounters;

    public Scene(String id, Image mImage, HashMap<Emotion, Long> mReactionCounters) {
        this.mId = id;
        this.mImage = mImage;
        this.mReactionCounters = mReactionCounters;
    }

    public Image getImage() {
        return mImage;
    }

    public long getReactionCounter(Emotion emotion) {
        return mReactionCounters.get(emotion);
    }

    public String getId() {
        return mId;
    }

    public void increaseCounter(Emotion emotion) {
        if (mReactionCounters.containsKey(emotion)) {
            mReactionCounters.put(emotion, mReactionCounters.get(emotion) + 1);
        } else {
            mReactionCounters.put(emotion, 1L);
        }
    }

    public HashMap<Emotion, Long> getReactionCounters() {
        return mReactionCounters;
    }
}
