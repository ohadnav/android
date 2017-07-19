package com.truethat.android.di.module.fake;

import com.truethat.android.application.storage.internal.FakeInternalStorageManager;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 18/07/2017 for TrueThat.
 */

@Module public class FakeInternalStorageModule {
  @Provides @AppScope InternalStorageManager provideInternalStorage() {
    return new FakeInternalStorageManager();
  }
}
