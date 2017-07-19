package com.truethat.android.di.component;

import com.truethat.android.di.scope.FragmentScope;
import com.truethat.android.ui.common.BaseFragment;
import com.truethat.android.ui.common.media.ReactableFragment;
import dagger.Component;

/**
 * Proudly created by ohad on 16/07/2017 for TrueThat.
 */

@FragmentScope @Component(dependencies = AppComponent.class)
public interface FragmentInjectorComponent {
  void inject(BaseFragment fragment);

  void inject(ReactableFragment reactableFragment);
}
