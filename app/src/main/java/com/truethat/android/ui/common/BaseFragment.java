package com.truethat.android.ui.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.truethat.android.R;

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
  private Unbinder mUnbinder;

  /**
   * Initializes root view and
   */
  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    mRootView = inflater.inflate(getLayoutResId(), container, false);
    mUnbinder = ButterKnife.bind(this, mRootView);
    return mRootView;
  }

  /**
   * @return the fragment layout resource ID, as found in {@link R.layout}.
   */
  protected abstract int getLayoutResId();

  @Override public void onDestroyView() {
    super.onDestroyView();
    mUnbinder.unbind();
  }

  /**
   * User visibility is set regardless of fragment lifecycle, and so we invoke {@link #onVisible()}
   * and {@link #onHidden()} here as well.
   */
  @Override public void onResume() {
    super.onResume();
    if (getUserVisibleHint()) onVisible();
  }

  @Override public void onPause() {
    super.onPause();
    onHidden();
  }

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

  /**
   * Should be invoked once this fragment is visible. Use with caution.
   */
  protected void onVisible() {
  }

  /**
   * Should be invoked once this fragment is hidden. Use with caution.
   */
  protected void onHidden() {
  }
}
