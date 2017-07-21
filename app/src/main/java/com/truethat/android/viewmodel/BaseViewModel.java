package com.truethat.android.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.truethat.android.di.component.ViewModelInjectorComponent;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.AbstractViewModel;
import javax.inject.Inject;
import retrofit2.Retrofit;

/**
 * Proudly created by ohad on 19/07/2017 for TrueThat.
 */

public class BaseViewModel<ViewInterface extends BaseViewInterface>
    extends AbstractViewModel<ViewInterface> {
  /**
   * Logging tag. Assigned per implementing class in {@link #onCreate(Bundle, Bundle)}.
   */
  protected String TAG = this.getClass().getSimpleName();

  @Inject Context mContext;
  @Inject Retrofit mRetrofit;
  @Inject Gson mGson;

  @Override
  public final void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    TAG = this.getClass().getSimpleName();
  }

  @SuppressWarnings("ConstantConditions") @NonNull @Override public ViewInterface getView() {
    return super.getView();
  }

  @SuppressWarnings("unchecked") @CallSuper
  public void inject(ViewModelInjectorComponent injector) {
    injector.inject((BaseViewModel<BaseViewInterface>) this);
    onInjected();
  }

  public void onInjected() {

  }

  <T> T createApiInterface(final Class<T> service) {
    return mRetrofit.create(service);
  }
}
