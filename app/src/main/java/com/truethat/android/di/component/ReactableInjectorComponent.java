package com.truethat.android.di.component;

import com.truethat.android.di.module.ReactableModule;
import com.truethat.android.di.scope.ReactableScope;
import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.ReactableViewModel;
import dagger.Component;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 */

@ReactableScope @Component(modules = ReactableModule.class, dependencies = AppComponent.class)
public interface ReactableInjectorComponent {
  void inject(ReactableViewModel<Reactable> reactableViewModel);
}
