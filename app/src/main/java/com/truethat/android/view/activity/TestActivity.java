package com.truethat.android.view.activity;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.google.gson.Gson;
import com.truethat.android.R;
import com.truethat.android.application.DeviceManager;
import com.truethat.android.application.auth.AuthManager;
import com.truethat.android.databinding.ActivityTestBinding;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

@VisibleForTesting public class TestActivity
    extends BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityTestBinding>
    implements CameraFragment.OnPictureTakenListener {

  @Override public void processImage(Image image) {
  }

  public AuthManager getAuthManager() {
    return mAuthManager;
  }

  public DeviceManager getDeviceManager() {
    return mDeviceManager;
  }

  public Gson getGson() {
    return mGson;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_test, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    // Should not authenticate on tests.
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
  }
}
