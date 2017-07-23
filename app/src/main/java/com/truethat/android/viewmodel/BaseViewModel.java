package com.truethat.android.viewmodel;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.di.component.ViewModelInjectorComponent;
import com.truethat.android.empathy.ReactionDetectionManager;
import com.truethat.android.model.User;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.AbstractViewModel;
import javax.inject.Inject;
import javax.inject.Provider;
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
  @Inject Provider<User> mCurrentUser;
  @Inject DeviceManager mDeviceManager;
  @Inject AuthManager mAuthManager;
  @Inject ReactionDetectionManager mDetectionManager;

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

  public Provider<User> getCurrentUser() {
    return mCurrentUser;
  }

  public DeviceManager getDeviceManager() {
    return mDeviceManager;
  }

  public Gson getGson() {
    return mGson;
  }

  public AuthManager getAuthManager() {
    return mAuthManager;
  }

  ReactionDetectionManager getDetectionManager() {
    return mDetectionManager;
  }

  <T> T createApiInterface(final Class<T> service) {
    return mRetrofit.create(service);
  }
}
