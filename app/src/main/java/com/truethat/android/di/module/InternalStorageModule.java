package com.truethat.android.di.module;

import android.content.Context;
import com.truethat.android.application.storage.internal.DefaultInternalStorageManager;
import com.truethat.android.application.storage.internal.InternalStorageManager;
import com.truethat.android.di.scope.AppScope;
import dagger.Module;
import dagger.Provides;

/**
 * Proudly created by ohad on 17/07/2017 for TrueThat.
 */

@Module(includes = AppModule.class) public class InternalStorageModule {
  @Provides @AppScope InternalStorageManager provideInternalStorage(Context context) {
    return new DefaultInternalStorageManager(context);
  }
}
