package com.truethat.android.di.component;

import com.truethat.android.di.scope.ActivityScope;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import dagger.Component;

/**
 * Proudly created by ohad on 20/07/2017 for TrueThat.
 */

@ActivityScope @Component(dependencies = AppComponent.class)
public interface ViewModelInjectorComponent {
  void inject(BaseViewModel<BaseViewInterface> viewModel);
}
