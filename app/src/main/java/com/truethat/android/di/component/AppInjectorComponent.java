package com.truethat.android.di.component;

import android.databinding.ViewDataBinding;
import com.truethat.android.di.scope.ActivityScope;
import com.truethat.android.view.activity.BaseActivity;
import com.truethat.android.view.fragment.BaseFragment;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import dagger.Component;

/**
 * Proudly created by ohad on 16/07/2017 for TrueThat.
 *
 * Dependency injection component for activities, fragments and view models.
 */

@ActivityScope @Component(dependencies = AppComponent.class) public interface AppInjectorComponent {
  void inject(
      BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ViewDataBinding> activity);

  void inject(
      BaseFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, ViewDataBinding> fragment);

  void inject(BaseViewModel<BaseViewInterface> viewModel);

  void inject(BaseFragmentViewModel<BaseFragmentViewInterface> viewModel);
}
