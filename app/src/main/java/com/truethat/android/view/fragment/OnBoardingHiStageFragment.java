package com.truethat.android.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.application.permissions.PermissionListener;
import com.truethat.android.databinding.FragmentOnBoardingHiBinding;
import com.truethat.android.view.activity.OnBoardingActivity;
import com.truethat.android.viewmodel.BaseFragmentViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseFragmentViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

/**
 * Proudly created by ohad on 25/10/2017 for TrueThat.
 */

public class OnBoardingHiStageFragment extends
    OnBoardingStageFragment<BaseFragmentViewInterface, BaseFragmentViewModel<BaseFragmentViewInterface>, FragmentOnBoardingHiBinding>
    implements PermissionListener {


  public static OnBoardingHiStageFragment newInstance() {
    Bundle args = new Bundle();
    OnBoardingHiStageFragment fragment = new OnBoardingHiStageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_on_boarding_hi, getContext());
  }

  @Override public void onVisible() {
    super.onVisible();
    getBaseActivity().addPermissionListener(this);
  }

  @Override public void onHidden() {
    super.onHidden();
    getBaseActivity().removePermissionListener(this);
  }

  @Override public void onPermissionGranted(Permission permission) {
    mOnBoardingListener.onComplete(OnBoardingActivity.HI_STAGE_INDEX);
  }

  @Override public void onPermissionRejected(Permission permission) {
    if (permission == Permission.CAMERA) {
      getBaseActivity().startAskForPermissionActivity(permission);
    }
  }

  @OnClick(R.id.onBoarding_hiButton) void askForPermission() {
    if (AppContainer.getPermissionsManager().isPermissionGranted(Permission.CAMERA)) {
      mOnBoardingListener.onComplete(OnBoardingActivity.HI_STAGE_INDEX);
    } else {
      AppContainer.getPermissionsManager().requestIfNeeded(getBaseActivity(), Permission.CAMERA);
    }
  }
}
