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
import com.truethat.android.external.viewmodel.ProxyViewHelper;
import com.truethat.android.external.viewmodel.ViewModelHelper;
import com.truethat.android.view.activity.BaseActivity;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import com.truethat.android.viewmodel.viewinterface.BaseListener;

/**
 * Proudly created by ohad on 22/06/2017 for TrueThat.
 */

public abstract class BaseFragment<ViewInterface extends BaseFragmentViewInterface, ViewModel extends BaseFragmentViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends Fragment
    implements BaseFragmentViewInterface, BaseListener, View.OnAttachStateChangeListener,
    VisibilityListener {
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
   * Unbinds views, to prevent memory leaks.
   */
  Unbinder mViewUnbinder;
  /**
   * Whether to let {@link BaseFragment} handle Butterknife's view binding.
   */
  boolean mAutomaticViewBinding = true;
  /**
   * Visibility interface for nested fragments. Useful when the base class "isVisible" efforts are
   * ambiguous.
   */
  VisibilityListener mVisibilityListener;
  VisibilityState mVisibilityState = VisibilityState.HIDDEN;

  public VisibilityListener getVisibilityListener() {
    return mVisibilityListener;
  }

  public void setVisibilityListener(VisibilityListener visibilityListener) {
    mVisibilityListener = visibilityListener;
    maybeChangeVisibilityState();
  }

  public void maybeChangeVisibilityState() {
    if (!isResumed()) {
      if (mVisibilityState == VisibilityState.VISIBLE) {
        onHidden();
      }
    } else if (mVisibilityListener != null) {
      if (probablyVisible() && mVisibilityState != VisibilityState.VISIBLE) {
        onVisible();
      } else if (mVisibilityState != VisibilityState.HIDDEN) {
        if (!isVisible() || !getUserVisibleHint() || !mVisibilityListener.shouldBeVisible(this)) {
          onHidden();
        }
      }
    }
  }

  /**
   * Should be invoked once this fragment is visible. Use with caution.
   */
  @SuppressWarnings("WeakerAccess") @CallSuper public void onVisible() {
    Log.d(TAG, "onVisible");
    mVisibilityState = VisibilityState.VISIBLE;
    getViewModel().onVisible();
  }

  /**
   * Should be invoked once this fragment is hidden. Use with caution.
   */
  @SuppressWarnings("WeakerAccess") @CallSuper public void onHidden() {
    Log.d(TAG, "onHidden");
    mVisibilityState = VisibilityState.HIDDEN;
    getViewModel().onHidden();
  }

  /**
   * @return whether this fragment is resumed and visible to the user.
   */
  public boolean isVisibleAndResumed() {
    return probablyVisible() && isResumed() && mVisibilityState == VisibilityState.VISIBLE;
  }

  @Override public void toast(String text) {
    getBaseActivity().toast(text);
  }

  @Override public BaseActivity getBaseActivity() {
    return (BaseActivity) getActivity();
  }

  @Override public String getTAG() {
    return TAG;
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
    mViewModelHelper.removeViewModel(getBaseActivity());
  }

  @Override public String toString() {
    return TAG;
  }

  @Override public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);
    maybeChangeVisibilityState();
  }

  @Override public void onAttach(Context context) {
    try {
      // Tries to specify TAG.
      if (!TAG.contains("(")) {
        TAG += "(" + getResources().getResourceEntryName(getId()) + ")";
      }
    } catch (Exception ignored) {
    }
    Log.d(TAG, "onAttach");
    super.onAttach(context);
  }

  @SuppressWarnings("unchecked") @CallSuper @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    // Initializes view model
    Class<ViewModel> viewModelClass =
        (Class<ViewModel>) ProxyViewHelper.getGenericType(getClass(), BaseFragmentViewModel.class);

    mViewModelHelper.onCreate(getBaseActivity(), savedInstanceState, viewModelClass,
        getArguments());
    mViewModelHelper.performBinding(this);
  }

  /**
   * Initializes root view and
   */
  @SuppressWarnings("unchecked") @CallSuper @Nullable @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView");
    // Completes data binding.
    mViewModelHelper.performBinding(this);
    final ViewDataBinding binding = mViewModelHelper.getBinding();
    if (binding == null) {
      throw new IllegalStateException(
          "Binding cannot be null. Perform binding before calling getBinding()");
    }
    View rootView = binding.getRoot();
    if (rootView == null) {
      throw new IllegalStateException("Fragment root view must be initialized.");
    }
    // Sets the view interface.
    setModelView((ViewInterface) this);
    if (mAutomaticViewBinding) {
      // Binds views with butterknife.
      mViewUnbinder = ButterKnife.bind(this, rootView);
    }
    // Sets up context
    mViewModelHelper.getViewModel().setContext(this.getActivity());
    rootView.addOnAttachStateChangeListener(this);
    return rootView;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    Log.d(TAG, "onViewCreated");
    super.onViewCreated(view, savedInstanceState);
  }

  @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    getViewModel().onRestoreInstanceState(savedInstanceState);
  }

  @CallSuper @Override public void onStart() {
    Log.d(TAG, "onStart");
    super.onStart();
    mViewModelHelper.onStart();
  }

  /**
   * User visibility is set regardless of fragment lifecycle, and so we invoke {@link #onVisible()}
   * and {@link #onHidden()} here as well.
   */
  @CallSuper @Override public void onResume() {
    Log.d(TAG, "onResume");
    super.onResume();
    getViewModel().onResume();
    maybeChangeVisibilityState();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    Log.d(TAG, "onSaveInstanceState");
    super.onSaveInstanceState(outState);
    mViewModelHelper.onSaveInstanceState(outState);
  }

  @CallSuper @Override public void onPause() {
    maybeChangeVisibilityState();
    Log.d(TAG, "onPause");
    super.onPause();
    getViewModel().onPause();
  }

  @CallSuper @Override public void onStop() {
    Log.d(TAG, "onStop");
    super.onStop();
    mViewModelHelper.onStop();
  }

  @CallSuper @Override public void onDestroyView() {
    Log.d(TAG, "onDestroyView");
    mViewModelHelper.onDestroyView(this);
    super.onDestroyView();
    mViewUnbinder.unbind();
  }

  @CallSuper @Override public void onDestroy() {
    Log.d(TAG, "onDestroy");
    mViewModelHelper.onDestroy(this);
    super.onDestroy();
  }

  @Override public void onDetach() {
    Log.d(TAG, "onDetach");
    super.onDetach();
  }

  @Override public void onViewAttachedToWindow(View v) {
    Log.d(TAG, "onViewAttachedToWindow");
    maybeChangeVisibilityState();
  }

  @Override public void onViewDetachedFromWindow(View v) {
    Log.d(TAG, "onViewDetachedToWindow");
    maybeChangeVisibilityState();
  }

  @Override public boolean shouldBeVisible(Object o) {
    if (o instanceof Fragment || o instanceof View) {
      int id = o instanceof Fragment ? ((Fragment) o).getId() : ((View) o).getId();
      return isVisibleAndResumed() && getView() != null && getView().findViewById(id) != null;
    }
    return false;
  }

  /**
   * @return trying to asses if the fragment is visible. Considering visibility hint, visibility
   * interface (useful for fragments nested in pagers), and base class {@link #isVisible()}
   */
  private boolean probablyVisible() {
    return getUserVisibleHint() && mVisibilityListener != null && isVisible() && mVisibilityListener
        .shouldBeVisible(this);
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

  private enum VisibilityState {
    VISIBLE, HIDDEN
  }
}
