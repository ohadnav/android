package com.truethat.android.di.module.fake;

import com.truethat.android.di.scope.AppScope;
import com.truethat.android.empathy.FakeReactionDetectionManager;
import com.truethat.android.empathy.ReactionDetectionManager;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

@Module public class FakeReactionDetectionModule {
  @Provides @AppScope ReactionDetectionManager provideReactionDetectionManager() {
    return new FakeReactionDetectionManager();
  }
}
