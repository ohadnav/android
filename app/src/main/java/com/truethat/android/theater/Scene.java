package com.truethat.android.theater;

import com.truethat.android.application.App;
import com.truethat.android.common.Emotion;
import com.truethat.android.identity.User;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Proudly created by ohad on 01/05/2017 for TrueThat.
 */

public class Scene implements Serializable {
    /**
     * Scene ID, as stored in our backend.
     */
    private long   mId;
    /**
     * Byte array that can be converted to the scene's image. Image instance is not used, as it is
     * not serializable, nor can it be used for UI.
     */
    private byte[] mImageBytes;
    // By default the current user is considered the creator.
    private User mCreator = App.getAuthModule().getCurrentUser();

    private Map<Emotion, Long> mReactionCounters = new HashMap<>();

    private long mViewCount = 0;
    private Date mTimestamp = new Date();

    public Scene(byte[] imageBytes) {
        this.mImageBytes = imageBytes;
    }

    public Date getTimestamp() {
        return mTimestamp;
    }

    public byte[] getImageBytes() {
        return mImageBytes;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public User getCreator() {
        return mCreator;
    }

    public Map<Emotion, Long> getReactionCounters() {
        return mReactionCounters;
    }

    public long getViewCount() {
        return mViewCount;
    }
}
