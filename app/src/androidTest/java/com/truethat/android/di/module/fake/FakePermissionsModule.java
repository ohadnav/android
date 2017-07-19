package com.truethat.android.di.module.fake;

import com.truethat.android.application.permissions.FakePermissionsManager;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 17/07/2017 for TrueThat.
 */

@Module public class FakePermissionsModule {
  @Provides @AppScope PermissionsManager providePermissionsManager() {
    return new FakePermissionsManager();
  }
}
