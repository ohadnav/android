package com.truethat.android.external.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import com.truethat.android.view.activity.BaseActivity;
import com.truethat.android.view.fragment.BaseFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.BuildConfig;
import eu.inloop.viewmodel.IView;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.UUID;

/**
 * Proudly created by ohad on 17/10/2017 for TrueThat.
 */

public class ViewModelHelper<ViewInterface extends BaseViewInterface, ViewModel extends BaseViewModel<ViewInterface>> {

  @NonNull private static final String STATE_STRING_SCREEN_IDENTIFIER =
      eu.inloop.viewmodel.ViewModelHelper.class + ".state.string.identifier"; //NON-NLS

  @Nullable private String mScreenId;

  @Nullable private ViewModel mViewModel;

  @Nullable private ViewDataBinding mBinding;

  private boolean mModelRemoved;
  private boolean mOnSaveInstanceCalled;

  /**
   * Call from {@link android.app.Activity#onCreate(android.os.Bundle)} or
   * {@link android.support.v4.app.Fragment#onCreate(android.os.Bundle)}
   *
   * @param activity           parent activity
   * @param savedInstanceState savedInstance state from {@link Activity#onCreate(Bundle)} or
   *                           {@link Fragment#onCreate(Bundle)}
   * @param viewModelClass     the {@link Class} of your ViewModel
   * @param arguments          pass {@link Fragment#getArguments()}  or
   *                           {@link Activity#getIntent()}.{@link Intent#getExtras() getExtras()}
   */
  public void onCreate(@NonNull BaseActivity activity, @Nullable Bundle savedInstanceState,
      @Nullable Class<? extends BaseViewModel<ViewInterface>> viewModelClass,
      @Nullable Bundle arguments) {
    // no viewmodel for this fragment
    if (viewModelClass == null) {
      mViewModel = null;
      return;
    }

    // screen (activity/fragment) created for first time, attach unique ID
    if (savedInstanceState == null) {
      mScreenId = UUID.randomUUID().toString();
    } else {
      mScreenId = savedInstanceState.getString(STATE_STRING_SCREEN_IDENTIFIER);
      if (null == mScreenId) {
        throw new IllegalStateException(
            "Bundle from onSaveInstanceState() didn't contain screen identifier. " + //NON-NLS
                "Did you call ViewModelHelper.onSaveInstanceState?"); //NON-NLS
      }

      mOnSaveInstanceCalled = false;
    }

    // get model instance for this screen
    final ViewModelProvider viewModelProvider = activity.getViewModelProvider();
    if (null == viewModelProvider) {
      throw new IllegalStateException(
          "ViewModelProvider for activity " + activity + " was null."); //NON-NLS
    }

    final ViewModelProvider.ViewModelWrapper<ViewInterface> viewModelWrapper =
        viewModelProvider.getViewModel(mScreenId, viewModelClass);
    //noinspection unchecked
    mViewModel = (ViewModel) viewModelWrapper.viewModel;

    // detect that the system has killed the app - saved instance is not null, but the model was recreated
    if (BuildConfig.DEBUG && savedInstanceState != null) {
      Log.d("model",
          "Fragment recreated by system or ViewModelStatePagerAdapter - restoring viewmodel"); //NON-NLS
    }
    mViewModel.onCreate(arguments, savedInstanceState);
  }

  /**
   * Call from {@link BaseFragment#onViewCreated(android.view.View, android.os.Bundle)}
   * or {@link android.app.Activity#onCreate(android.os.Bundle)}
   *
   * @param view view
   */
  public void setView(@NonNull final ViewInterface view) {
    if (mViewModel == null) {
      //no viewmodel for this fragment
      return;
    }
    mViewModel.onBindView(view);
  }

  public void performBinding(@NonNull final IView bindingView) {
    // skip if already create
    if (mBinding != null) {
      return;
    }

    // get ViewModelBinding config
    final ViewModelBindingConfig viewModelConfig = bindingView.getViewModelBindingConfig();
    // if fragment not providing ViewModelBindingConfig, do not perform binding operations
    if (viewModelConfig == null) {
      return;
    }

    // perform Data Binding initialization
    final ViewDataBinding viewDataBinding;
    if (bindingView instanceof Activity) {
      viewDataBinding = DataBindingUtil.setContentView(((Activity) bindingView),
          viewModelConfig.getLayoutResource());
    } else if (bindingView instanceof Fragment) {
      viewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(viewModelConfig.getContext()),
          viewModelConfig.getLayoutResource(), null, false);
    } else {
      throw new IllegalArgumentException(
          "View must be an instance of Activity or Fragment (support-v4).");
    }

    // bind all together
    if (!viewDataBinding.setVariable(viewModelConfig.getViewModelVariableName(), getViewModel())) {
      throw new IllegalArgumentException(
          "Binding variable wasn't set successfully. Probably viewModelVariableName of your "
              + "ViewModelBindingConfig of "
              + bindingView.getClass().getSimpleName()
              + " doesn't match any variable in "
              + viewDataBinding.getClass().getSimpleName());
    }

    mBinding = viewDataBinding;
  }

  /**
   * Use in case this model is associated with an {@link android.support.v4.app.Fragment}
   * Call from {@link android.support.v4.app.Fragment#onDestroyView()}. Use in case model is
   * associated
   * with Fragment
   *
   * @param fragment fragment
   */
  public void onDestroyView(@NonNull BaseFragment fragment) {
    if (mViewModel == null) {
      //no viewmodel for this fragment
      return;
    }
    mViewModel.clearView();
    if (fragment.getBaseActivity() != null && fragment.getActivity().isFinishing()) {
      removeViewModel(fragment.getBaseActivity());
    }
    mBinding = null;
  }

  /**
   * Use in case this model is associated with an {@link android.support.v4.app.Fragment}
   * Call from {@link android.support.v4.app.Fragment#onDestroy()}
   *
   * @param fragment fragment
   */
  public void onDestroy(@NonNull final BaseFragment fragment) {
    if (mViewModel == null) {
      //no viewmodel for this fragment
      return;
    }
    if (fragment.getActivity().isFinishing()) {
      removeViewModel(fragment.getBaseActivity());
    } else if (fragment.isRemoving() && !mOnSaveInstanceCalled) {
      // The fragment can be still in backstack even if isRemoving() is true.
      // We check mOnSaveInstanceCalled - if this was not called then the fragment is totally removed.
      if (BuildConfig.DEBUG) {
        Log.d("mode", "Removing viewmodel - fragment replaced"); //NON-NLS
      }
      removeViewModel(fragment.getBaseActivity());
    }
    mBinding = null;
  }

  /**
   * Use in case this model is associated with an {@link android.app.Activity}
   * Call from {@link android.app.Activity#onDestroy()}
   *
   * @param activity activity
   */
  public void onDestroy(@NonNull final BaseActivity activity) {
    if (mViewModel == null) {
      //no viewmodel for this fragment
      return;
    }
    mViewModel.clearView();
    if (activity.isFinishing()) {
      removeViewModel(activity);
    }
    mBinding = null;
  }

  /**
   * Call from {@link android.app.Activity#onStop()} or {@link android.support.v4.app.Fragment#onStop()}
   */
  public void onStop() {
    if (mViewModel == null) {
      //no viewmodel for this fragment
      return;
    }
    mViewModel.onStop();
  }

  /**
   * Call from {@link android.app.Activity#onStart()} ()} or {@link android.support.v4.app.Fragment#onStart()}
   * ()}
   */
  public void onStart() {
    if (mViewModel == null) {
      //no viewmodel for this fragment
      return;
    }
    mViewModel.onStart();
  }

  /**
   * Returns the current ViewModel instance associated with the Fragment or Activity.
   * Throws an {@link IllegalStateException} in case the ViewModel is null. This can happen
   * if you call this method too soon - before {@link Activity#onCreate(Bundle)} or {@link
   * Fragment#onCreate(Bundle)}
   * or this {@link eu.inloop.viewmodel.ViewModelHelper} is not properly setup.
   *
   * @return {@link ViewModel}
   */
  @NonNull public ViewModel getViewModel() {
    if (null == mViewModel) {
      throw new IllegalStateException(
          "ViewModel is not ready. Are you calling this method before Activity/Fragment onCreate?"); //NON-NLS
    }
    return mViewModel;
  }

  /**
   * Call from {@link android.app.Activity#onSaveInstanceState(android.os.Bundle)}
   * or {@link android.support.v4.app.Fragment#onSaveInstanceState(android.os.Bundle)}.
   * This allows the model to save its state.
   *
   * @param bundle bundle
   */
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    bundle.putString(STATE_STRING_SCREEN_IDENTIFIER, mScreenId);
    if (mViewModel != null) {
      mViewModel.onSaveInstanceState(bundle);
      mOnSaveInstanceCalled = true;
    }
  }

  @Nullable public ViewDataBinding getBinding() {
    return mBinding;
  }

  public void removeViewModel(@NonNull final BaseActivity activity) {
    if (mViewModel != null && !mModelRemoved) {
      final ViewModelProvider viewModelProvider = activity.getViewModelProvider();
      if (null == viewModelProvider) {
        throw new IllegalStateException(
            "ViewModelProvider for activity " + activity + " was null."); //NON-NLS
      }
      viewModelProvider.remove(mScreenId);
      mViewModel.onDestroy();
      mModelRemoved = true;
      mBinding = null;
    }
  }
}
