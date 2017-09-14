package com.truethat.android.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.AbstractViewModel;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

public class BaseViewModel<ViewInterface extends BaseViewInterface>
    extends AbstractViewModel<ViewInterface> {
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle, Bundle)}.
   */
  String TAG = this.getClass().getSimpleName();
  private Context mContext;

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    TAG = this.getClass().getSimpleName();
    Log.d(TAG, "CREATED");
  }

  @Override public void onBindView(@NonNull ViewInterface view) {
    super.onBindView(view);
    TAG = this.getClass().getSimpleName() + "(" + getView().getClass().getSimpleName() + ")";
    Log.d(TAG, "DATA-BOUND");
  }

  @SuppressWarnings({ "ConstantConditions", "EmptyMethod" }) @NonNull @Override
  public ViewInterface getView() {
    return super.getView();
  }

  @Override public void onStop() {
    Log.d(TAG, "STOPPED");
    super.onStop();
  }

  @Override public void onStart() {
    Log.d(TAG, "STARTED");
    super.onStart();
  }

  Context getContext() {
    return mContext;
  }

  public void setContext(Context context) {
    mContext = context;
  }
}
