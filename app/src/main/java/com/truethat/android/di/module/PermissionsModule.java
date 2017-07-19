package com.truethat.android.di.module;

import android.content.Context;
import com.truethat.android.application.permissions.DefaultPermissionsManager;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 17/07/2017 for TrueThat.
 */

@Module(includes = AppModule.class) public class PermissionsModule {
  @Provides @AppScope PermissionsManager providePermissionsManager(Context context) {
    return new DefaultPermissionsManager(context);
  }
}
