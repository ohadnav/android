package com.truethat.android.di.component;

import com.truethat.android.di.module.ReactableModule;
import com.truethat.android.di.scope.ReactableScope;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.fragment.ReactableFragment;
import com.truethat.android.viewmodel.ReactableViewModel;
import dagger.Component;

/**
 * Proudly created by ohad on 23/07/2017 for TrueThat.
 *
 * Injector for {@link ReactableFragment}, so that they can have a less restrictive scope.
 */

@ReactableScope @Component(modules = ReactableModule.class, dependencies = AppComponent.class)
public interface ReactableInjectorComponent {
  void inject(ReactableViewModel<Reactable> reactableViewModel);
}
