package com.truethat.android.identity;

import android.support.annotation.VisibleForTesting;

import java.io.Serializable;

/**
 * Proudly created by ohad on 22/05/2017 for TrueThat.
 */

public class User implements Serializable {
    /**
     * User ID as stored in our backend.
     */
    private long mId;

    @VisibleForTesting
    User(long id) {
        mId = id;
    }

    // TODO(ohad): retrieve logged in user from internal storage.
    User() {
    }

    public long getId() {
        return mId;
    }
}
