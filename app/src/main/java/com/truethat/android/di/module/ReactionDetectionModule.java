package com.truethat.android.di.module;

import android.content.Context;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.di.scope.AppScope;
import com.truethat.android.empathy.DefaultReactionDetectionManager;
import com.truethat.android.empathy.ReactionDetectionManager;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

@Module public class ReactionDetectionModule {
  @Provides @AppScope ReactionDetectionManager provideReactionDetectionManager(
      PermissionsManager permissionsManager, Context context) {
    return new DefaultReactionDetectionManager(permissionsManager, context);
  }
}
