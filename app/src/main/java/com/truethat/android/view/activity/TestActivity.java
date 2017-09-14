package com.truethat.android.view.activity;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.truethat.android.R;
import com.truethat.android.databinding.ActivityTestBinding;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

@VisibleForTesting public class TestActivity
    extends BaseActivity<BaseViewInterface, BaseViewModel<BaseViewInterface>, ActivityTestBinding>
    implements CameraFragment.CameraFragmentListener {

  @Override public void onPhotoTaken(Image image) {
  }

  @Override public void onVideoRecorded(String videoPath) {

  }

  @Override public void onVideoRecordStart() {

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
