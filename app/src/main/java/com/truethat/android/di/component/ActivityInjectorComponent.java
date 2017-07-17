package com.truethat.android.di.component;

import com.truethat.android.di.scope.ActivityScope;
import com.truethat.android.ui.activity.BaseActivity;
import dagger.Component;

/**
 * Proudly created by ohad on 16/07/2017 for TrueThat.
 */

@ActivityScope @Component(dependencies = BaseComponent.class)
public interface ActivityInjectorComponent {
  void inject(BaseActivity activity);
}
