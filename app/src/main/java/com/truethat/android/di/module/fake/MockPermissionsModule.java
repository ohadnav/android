package com.truethat.android.di.module.fake;

import android.app.Activity;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

@Module public class MockPermissionsModule {
  @Provides @AppScope PermissionsManager providePermissionsManager() {
    return new PermissionsManager() {
      @Override public boolean isPermissionGranted(Permission permission) {
        return true;
      }

      @Override public void requestIfNeeded(Activity activity, Permission permission) {

      }
    };
  }
}
