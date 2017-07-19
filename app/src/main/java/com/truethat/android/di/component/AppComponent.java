package com.truethat.android.di.component;

import com.google.gson.Gson;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.application.permissions.PermissionsManager;
import com.truethat.android.di.module.AppModule;
import com.truethat.android.di.module.AuthModule;
import com.truethat.android.di.module.DefaultAuthModule;
import com.truethat.android.di.module.DeviceModule;
import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.module.PermissionsModule;
import com.truethat.android.di.module.ReactionDetectionModule;
import com.truethat.android.di.scope.AppScope;
import com.truethat.android.empathy.ReactionDetectionManager;
import com.truethat.android.model.User;
import dagger.Component;
import javax.inject.Singleton;
import retrofit2.Retrofit;

/**
 * Proudly created by ohad on 16/07/2017 for TrueThat.
 */
@Component(modules = {
    AppModule.class, NetModule.class, PermissionsModule.class, DeviceModule.class, AuthModule.class,
    DefaultAuthModule.class, ReactionDetectionModule.class
}) @Singleton @AppScope public interface AppComponent {
  Gson gson();

  Retrofit retrofit();

  PermissionsManager permissionsManager();

  DeviceManager deviceManager();

  AuthManager authManager();

  ReactionDetectionManager reactionDetectionManager();

  User user();
}
