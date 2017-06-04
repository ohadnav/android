package com.truethat.android.common;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.truethat.android.application.App;
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
    @SerializedName("id")
    private long   mId;

    /**
     * Byte array that can be converted to the scene's image. Image instance is not used, as it is
     * not serializable, nor can it be used for UI purposes.
     */
    @Expose(serialize = false, deserialize = false)
    private byte[] mImageBytes;

    /**
     * Signed URL to the scene's image on our storage.
     */
    @SerializedName("image_signed_url")
    private String mImageSignedUrl;

    /**
     * Creator of the scene. By default, the current user is assigned.
     */
    @SerializedName("director")
    private User mDirector = App.getAuthModule().getCurrentUser();

    /**
     * Counters of emotional reactions to the scene, per each emotion.
     */
    @SerializedName("reaction_counters")
    private Map<Emotion, Long> mReactionCounters = new HashMap<>();

    /**
     * Count of users that have viewed the scene.
     */
    @Expose(serialize = false)
    @SerializedName("view_count")
    private long mViewCount = 0;

    /**
     * Date of scene creation.
     */
    @SerializedName("created")
    private Date mCreated = new Date();

    public Scene(byte[] imageBytes) {
        this.mImageBytes = imageBytes;
    }

    public Date getCreated() {
        return mCreated;
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

    public User getDirector() {
        return mDirector;
    }

    public Map<Emotion, Long> getReactionCounters() {
        return mReactionCounters;
    }

    public long getViewCount() {
        return mViewCount;
    }

    public String getImageSignedUrl() {
        return mImageSignedUrl;
    }
}
