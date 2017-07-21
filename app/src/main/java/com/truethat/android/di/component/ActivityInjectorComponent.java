package com.truethat.android.di.component;

import android.databinding.ViewDataBinding;
import com.truethat.android.di.scope.ActivityScope;
import com.truethat.android.view.activity.BaseActivity;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import dagger.Component;

/**
 * Proudly created by ohad on 16/07/2017 for TrueThat.
 */

@ActivityScope @Component(dependencies = AppComponent.class)
public interface ActivityInjectorComponent {
  void inject(
      BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ViewDataBinding> activity);
}
