package com.truethat.android.identity;

/**
 * Proudly created by ohad on 29/05/2017 for TrueThat.
 */

public class DefaultAuthModule implements AuthModule {
    private User mCurrentUser;

    public DefaultAuthModule() {
        // User default constructor is able to retrieve previous sessions.
        mCurrentUser = new User();
    }

    @Override
    public User getCurrentUser() {
        return mCurrentUser;
    }
}
