package com.truethat.android.di.component;

import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.AuthModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.fake.FakeAuthModule;
import com.truethat.android.di.module.fake.FakePermissionsModule;
import com.truethat.android.di.module.fake.FakeReactionDetectionModule;
import com.truethat.android.di.scope.AppScope;
import com.truethat.android.empathy.ReactionDetectionManager;
import dagger.Component;
import javax.inject.Singleton;
import retrofit2.Retrofit;

/**
 * Proudly created by ohad on 17/07/2017 for TrueThat.
 */

@Component(modules = {
    NetModule.class, AppModule.class, FakePermissionsModule.class, AuthModule.class,
    FakeAuthModule.class, FakeReactionDetectionModule.class
}) @Singleton @AppScope public interface TestAppComponent extends AppComponent {
  Retrofit retrofit();

  PermissionsManager permissionsManager();

  DeviceManager deviceManager();

  AuthManager authManager();

  ReactionDetectionManager reactionDetectionManager();
}
