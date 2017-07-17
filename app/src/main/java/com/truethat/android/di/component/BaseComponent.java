package com.truethat.android.di.component;

import com.truethat.android.di.module.NetModule;
import com.truethat.android.di.scope.ApplicationScope;
import dagger.Component;
import retrofit2.Retrofit;

/**
 * Proudly created by ohad on 16/07/2017 for TrueThat.
 */
@ApplicationScope @Component(modules = { NetModule.class }, dependencies = AppComponent.class)
public interface BaseComponent {
  Retrofit retrofit();
}
