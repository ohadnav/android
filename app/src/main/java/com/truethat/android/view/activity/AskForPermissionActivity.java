package com.truethat.android.view.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.permissions.Permission;
import com.truethat.android.databinding.ActivityAskForPermissionBinding;
import com.truethat.android.viewmodel.AskForPermissionViewModel;
import com.truethat.android.viewmodel.viewinterface.AskForPermissionViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AskForPermissionActivity extends
    BaseActivity<AskForPermissionViewInterface, AskForPermissionViewModel, ActivityAskForPermissionBinding> {
  public static final String EXTRA_PERMISSION = "permission";
  @BindView(R.id.rationaleText) TextView mRationaleText;
  private Permission mPermission;
  private Button mAskPermissionButton;

  /**
   * Asks for permission again.
   */
  @OnClick(R.id.askPermissionButton) public void askForPermission() {
    Log.v(TAG, "Asking for " + mPermission.name() + " again.");
    mPermissionsManager.requestIfNeeded(AskForPermissionActivity.this, mPermission);
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_ask_for_permission, this);
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    // Should not authenticate when asking for device permissions.
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
    mAskPermissionButton = (Button) findViewById(R.id.askPermissionButton);
  }

  @Override public void onResume() {
    super.onResume();
    // Obtaining the permission to which we want to ask for permission.
    mPermission = (Permission) getIntent().getExtras().get(EXTRA_PERMISSION);
    if (mPermission == null) {
      throw new AssertionError("Permission must be set, but is null.");
    }
    // Displays correct rationale
    mRationaleText.setText(mPermission.getRationaleText());
    // Ensure the button is revealed.
    mAskPermissionButton.bringToFront();
    // Check if permission is granted, and if so, finishes activity.
    if (mPermissionsManager.isPermissionGranted(mPermission)) {
      finish();
    }
  }

  /**
   * Overridden to invoke {@link #finishActivity(int)}
   */
  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    boolean allPermissionsGranted = true;
    for (int grantResult : grantResults) {
      if (grantResult != PERMISSION_GRANTED) allPermissionsGranted = false;
    }
    if (allPermissionsGranted) {
      finish();
    }
  }
}

