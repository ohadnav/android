package com.truethat.android.view.fragment;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.truethat.android.application.App;
import com.truethat.android.external.ProxyViewHelper;
import com.truethat.android.view.activity.BaseActivity;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.ViewModelHelper;
import javax.inject.Inject;

/**
 * Proudly created by ohad on 22/06/2017 for TrueThat.
 */

public abstract class BaseFragment<ViewInterface extends BaseFragmentViewInterface, ViewModel extends BaseFragmentViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends Fragment implements BaseFragmentViewInterface {
  /**
   * {@link BaseViewModel} manager of this fragment.
   */
  @NonNull private final ViewModelHelper<ViewInterface, ViewModel> mViewModelHelper =
      new ViewModelHelper<>();
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  String TAG = this.getClass().getSimpleName();
  /**
   * The fragment root view, that is inflated by
   * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  View mRootView;
  /**
   * Unbinds views, to prevent memory leaks.
   */
  private Unbinder mViewUnbinder;

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    if (isResumed()) {
      if (isVisibleToUser) {
        onVisible();
      } else {
        onHidden();
      }
    }
  }

  @Override public void onAttach(Context context) {
    TAG = this.getClass().getSimpleName() + "(" + getActivity().getClass().getSimpleName() + ")";
    Log.v(TAG, "ATTACHED");
    super.onAttach(context);
  }

  @SuppressWarnings("unchecked") @CallSuper @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initializes view model
    Class<ViewModel> viewModelClass =
        (Class<ViewModel>) ProxyViewHelper.getGenericType(getClass(), BaseViewModel.class);
    mViewModelHelper.onCreate(getActivity(), savedInstanceState, viewModelClass, getArguments());
    mViewModelHelper.performBinding(this);
  }

  /**
   * Initializes root view and
   */
  @SuppressWarnings("unchecked") @CallSuper @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.v(TAG, "VIEW CREATED");
    // Completes data binding.
    mViewModelHelper.performBinding(this);
    final ViewDataBinding binding = mViewModelHelper.getBinding();
    if (binding != null) {
      mRootView = binding.getRoot();
    } else {
      throw new IllegalStateException(
          "Binding cannot be null. Perform binding before calling getBinding()");
    }
    // Inject dependencies into the view model.
    getApp().getInjector().inject((BaseFragmentViewModel<BaseFragmentViewInterface>) getViewModel());
    getViewModel().onInjected();
    // Sets the view interface.
    setModelView((ViewInterface) this);
    // Binds views with butterknife.
    mViewUnbinder = ButterKnife.bind(this, mRootView);
    // Injects dependencies to this fragment.
    getApp().getInjector()
        .inject(
            (BaseFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, ViewDataBinding>) this);
    return mRootView;
  }

  @CallSuper @Override public void onStart() {
    Log.v(TAG, "STARTED");
    super.onStart();
    mViewModelHelper.onStart();
  }

  /**
   * User visibility is set regardless of fragment lifecycle, and so we invoke {@link #onVisible()}
   * and {@link #onHidden()} here as well.
   */
  @CallSuper @Override public void onResume() {
    Log.v(TAG, "RESUMED");
    super.onResume();
    if (getUserVisibleHint()) onVisible();
  }

  @CallSuper @Override public void onPause() {
    Log.v(TAG, "PAUSED");
    super.onPause();
    onHidden();
  }

  @CallSuper @Override public void onStop() {
    Log.v(TAG, "STOPPED");
    super.onStop();
    mViewModelHelper.onStop();
  }

  @CallSuper @Override public void onDestroyView() {
    Log.v(TAG, "VIEW DESTROYED");
    mViewModelHelper.onDestroyView(this);
    super.onDestroyView();
    mViewUnbinder.unbind();
  }

  @CallSuper @Override public void onDestroy() {
    mViewModelHelper.onDestroy(this);
    super.onDestroy();
  }

  /**
   * Should be invoked once this fragment is visible. Use with caution.
   */
  @SuppressWarnings("WeakerAccess") @CallSuper public void onVisible() {
    Log.v(TAG, "VISIBLE");
    getViewModel().onVisible();
  }

  /**
   * Should be invoked once this fragment is hidden. Use with caution.
   */
  @CallSuper public void onHidden() {
    Log.v(TAG, "HIDDEN");
    getViewModel().onHidden();
  }

  /**
   * @return whether this fragment is resumed and visible to the user.
   */
  public boolean isReallyVisible() {
    return getUserVisibleHint() && isResumed();
  }

  @Override public void toast(String text) {
    getBaseActivity().toast(text);
  }

  @SuppressWarnings({ "unused", "unchecked", "ConstantConditions" }) @NonNull
  public DataBinding getBinding() {
    try {
      return (DataBinding) mViewModelHelper.getBinding();
    } catch (ClassCastException ex) {
      throw new IllegalStateException("Method getViewModelBindingConfig() has to return same "
          + "ViewDataBinding type as it is set to base Fragment");
    }
  }

  /**
   * @see ViewModelHelper#getViewModel()
   */
  @NonNull @SuppressWarnings("unused") public ViewModel getViewModel() {
    return mViewModelHelper.getViewModel();
  }

  @Override public void removeViewModel() {
    mViewModelHelper.removeViewModel(getActivity());
  }

  @SuppressWarnings("unused") @Inject void logInjection() {
    Log.v(TAG, "INJECTED");
  }

  BaseActivity getBaseActivity() {
    return (BaseActivity) getActivity();
  }

  App getApp() {
    return getBaseActivity().getApp();
  }

  /**
   * Call this after your view is ready - usually on the end of {@link
   * Fragment#onViewCreated(View, Bundle)}
   *
   * @param viewInterface view
   */
  private void setModelView(@NonNull final ViewInterface viewInterface) {
    mViewModelHelper.setView(viewInterface);
  }
}
