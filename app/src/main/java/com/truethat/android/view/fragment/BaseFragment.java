package com.truethat.android.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.view.activity.BaseActivity;

/**
 * Proudly created by ohad on 22/06/2017 for TrueThat.
 */

public abstract class BaseFragment extends Fragment {
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle)}.
   */
  protected String TAG = this.getClass().getSimpleName();
  /**
   * The fragment root view, that is inflated by
   * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
   */
  protected View mRootView;
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

  @Override public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
    Log.v(TAG, "onInflate");
    super.onInflate(context, attrs, savedInstanceState);
  }

  @Override public void onAttach(Context context) {
    TAG = this.getClass().getSimpleName() + "(" + getActivity().getClass().getSimpleName() + ")";
    Log.v(TAG, "onAttach");
    super.onAttach(context);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    Log.v(TAG, "onCreate");
    super.onCreate(savedInstanceState);
  }

  /**
   * Initializes root view and
   */
  @CallSuper
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Log.v(TAG, "onCreateView");
    mRootView = inflater.inflate(getLayoutResId(), container, false);
    mViewUnbinder = ButterKnife.bind(this, mRootView);
    getApp().getFragmentInjector().inject(this);
    return mRootView;
  }

  @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    Log.v(TAG, "onViewStateRestored");
  }

  @Override public void onStart() {
    Log.v(TAG, "onStart");
    super.onStart();
  }

  /**
   * User visibility is set regardless of fragment lifecycle, and so we invoke {@link #onVisible()}
   * and {@link #onHidden()} here as well.
   */
  @CallSuper
  @Override public void onResume() {
    Log.v(TAG, "onResume");
    super.onResume();
    if (getUserVisibleHint()) onVisible();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    Log.v(TAG, "onSaveInstanceState");
  }

  @CallSuper
  @Override public void onPause() {
    Log.v(TAG, "onPause");
    super.onPause();
    onHidden();
  }

  @Override public void onStop() {
    Log.v(TAG, "onStop");
    super.onStop();
  }

  @CallSuper
  @Override public void onDestroyView() {
    Log.v(TAG, "onDestroyView");
    super.onDestroyView();
    mViewUnbinder.unbind();
  }

  @Override public void onDestroy() {
    Log.v(TAG, "onDestroy");
    super.onDestroy();
  }

  @Override public void onDetach() {
    Log.v(TAG, "onDetach");
    super.onDetach();
  }

  /**
   * Should be invoked once this fragment is visible. Use with caution.
   */
  public void onVisible() {
    Log.v(TAG, "onVisible");
  }

  /**
   * Should be invoked once this fragment is hidden. Use with caution.
   */
  public void onHidden() {
    Log.v(TAG, "onHidden");
  }

  /**
   * @return whether this fragment is resumed and visible to the user.
   */
  public boolean isReallyVisible() {
    return getUserVisibleHint() && isResumed();
  }

  public BaseActivity getBaseActivity() {
    return (BaseActivity) getActivity();
  }

  public App getApp() {
    return getBaseActivity().getApp();
  }

  /**
   * @return the fragment layout resource ID, as found in {@link R.layout}.
   */
  protected abstract @LayoutRes int getLayoutResId();
}
