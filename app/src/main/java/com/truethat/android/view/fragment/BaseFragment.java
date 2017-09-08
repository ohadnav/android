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
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.App;
import com.truethat.android.application.auth.AuthListener;
import com.truethat.android.external.ProxyViewHelper;
import com.truethat.android.view.activity.BaseActivity;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.ViewModelHelper;

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
  Unbinder mViewUnbinder;
  /**
   * Whether to let {@link BaseFragment} handle Butterknife's view binding.
   */
  boolean mAutomaticViewBinding = true;

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
    Log.d(TAG, "ATTACHED");
    super.onAttach(context);
  }

  @SuppressWarnings("unchecked") @CallSuper @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d(TAG, "CREATED");
    super.onCreate(savedInstanceState);
    // Initializes view model
    Class<ViewModel> viewModelClass =
        (Class<ViewModel>) ProxyViewHelper.getGenericType(getClass(), BaseFragmentViewModel.class);
    mViewModelHelper.onCreate(getActivity(), savedInstanceState, viewModelClass, getArguments());
    mViewModelHelper.performBinding(this);
  }

  /**
   * Initializes root view and
   */
  @SuppressWarnings("unchecked") @CallSuper @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.d(TAG, "CREATING VIEW");
    // Completes data binding.
    mViewModelHelper.performBinding(this);
    final ViewDataBinding binding = mViewModelHelper.getBinding();
    if (binding != null) {
      mRootView = binding.getRoot();
    } else {
      throw new IllegalStateException(
          "Binding cannot be null. Perform binding before calling getBinding()");
    }
    // Sets the view interface.
    setModelView((ViewInterface) this);
    if (mAutomaticViewBinding) {
      // Binds views with butterknife.
      mViewUnbinder = ButterKnife.bind(this, mRootView);
    }
    // Sets up context
    mViewModelHelper.getViewModel().setContext(this.getActivity());
    return mRootView;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "VIEW CREATED");
    super.onViewCreated(view, savedInstanceState);
  }

  @CallSuper @Override public void onStart() {
    Log.d(TAG, "STARTED");
    super.onStart();
    mViewModelHelper.onStart();
  }

  /**
   * User visibility is set regardless of fragment lifecycle, and so we invoke {@link #onVisible()}
   * and {@link #onHidden()} here as well.
   */
  @CallSuper @Override public void onResume() {
    Log.d(TAG, "RESUMED");
    super.onResume();
    if (getUserVisibleHint()) onVisible();
  }

  @CallSuper @Override public void onPause() {
    Log.d(TAG, "PAUSED");
    super.onPause();
    onHidden();
  }

  @CallSuper @Override public void onStop() {
    Log.d(TAG, "STOPPED");
    super.onStop();
    mViewModelHelper.onStop();
  }

  @CallSuper @Override public void onDestroyView() {
    Log.d(TAG, "VIEW DESTROYED");
    mViewModelHelper.onDestroyView(this);
    super.onDestroyView();
    mViewUnbinder.unbind();
  }

  @CallSuper @Override public void onDestroy() {
    Log.d(TAG, "DESTROYED");
    mViewModelHelper.onDestroy(this);
    super.onDestroy();
  }

  @Override public void onDetach() {
    Log.d(TAG, "DETACHED");
    super.onDetach();
  }

  /**
   * Should be invoked once this fragment is visible. Use with caution.
   */
  @SuppressWarnings("WeakerAccess") @CallSuper public void onVisible() {
    Log.d(TAG, "VISIBLE");
    getViewModel().onVisible();
  }

  /**
   * Should be invoked once this fragment is hidden. Use with caution.
   */
  @CallSuper public void onHidden() {
    Log.d(TAG, "HIDDEN");
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

  @Override public AuthListener getAuthListener() {
    return getBaseActivity();
  }

  @Override public BaseActivity getBaseActivity() {
    return (BaseActivity) getActivity();
  }

  @SuppressWarnings({ "unused", "unchecked", "ConstantConditions" }) @NonNull
  public DataBinding getBinding() {
    try {
      return (DataBinding) mViewModelHelper.getBinding();
    } catch (ClassCastException e) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(e);
      }
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
