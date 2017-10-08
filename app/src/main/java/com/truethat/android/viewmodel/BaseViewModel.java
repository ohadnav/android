package com.truethat.android.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.viewmodel.viewinterface.BaseListener;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.AbstractViewModel;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

public class BaseViewModel<ViewInterface extends BaseViewInterface>
    extends AbstractViewModel<ViewInterface> implements BaseListener {
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle, Bundle)}.
   */
  String TAG = this.getClass().getSimpleName();
  State mViewModelState = State.STOPPED;
  private Context mContext;

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    TAG = this.getClass().getSimpleName();
    Log.d(TAG, "CREATED");
  }

  @Override public void onBindView(@NonNull ViewInterface view) {
    super.onBindView(view);
    TAG = this.getClass().getSimpleName();
    if (getView() != null) {
      TAG += "(" + getView().getClass().getSimpleName() + ")";
    }
    Log.d(TAG, "DATA-BOUND");
  }

  @CallSuper @Override public void onStop() {
    Log.d(TAG, "STOPPED");
    mViewModelState = State.STOPPED;
    super.onStop();
  }

  @CallSuper @Override public void onStart() {
    Log.d(TAG, "STARTED");
    mViewModelState = State.STARTED;
    super.onStart();
  }

  @SuppressWarnings("UnusedParameters") public void onPermissionGranted(Permission permission) {

  }

  @Override public String getTAG() {
    return TAG;
  }

  @Override public String toString() {
    return TAG;
  }

  Context getContext() {
    return mContext;
  }

  public void setContext(Context context) {
    mContext = context;
  }

  enum State {
    /**
     * The view model had been started.
     */
    STARTED, /**
     * The view model was stopped.
     */
    STOPPED
  }
}
