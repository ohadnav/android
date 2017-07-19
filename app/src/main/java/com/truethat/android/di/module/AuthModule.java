package com.truethat.android.di.module;

import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.di.scope.AppScope;
import com.truethat.android.model.User;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 17/07/2017 for TrueThat.
 */
@Module public class AuthModule {
  @Provides @AppScope User provideUser(AuthManager authManager) {
    return authManager.currentUser();
  }
}
