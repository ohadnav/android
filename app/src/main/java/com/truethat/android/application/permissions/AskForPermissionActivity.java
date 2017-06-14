package com.truethat.android.application.permissions;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.BaseActivity;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AskForPermissionActivity extends BaseActivity {
  public static final String PERMISSION_EXTRA = "permission";
  private Permission mPermission;
  private Button mAskPermissionButton;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    // Should not authenticate when asking for device permissions.
    mSkipAuth = true;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ask_for_permission);
    mAskPermissionButton = (Button) this.findViewById(R.id.askPermissionButton);
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

  @Override protected void onResume() {
    super.onResume();
    // Obtaining the permission to which we want to ask for permission.
    mPermission = (Permission) getIntent().getExtras().get(PERMISSION_EXTRA);
    displayRationale();
    // Ensure the button is revealed.
    mAskPermissionButton.bringToFront();
    // Check if permission is granted, and if so, finishes activity.
    if (App.getPermissionsModule().isPermissionGranted(this, mPermission)) {
      finish();
    }
  }

  /**
   * Asks for permission again.
   */
  public void askForPermission(View view) {
    Log.v(TAG, "Asking for " + mPermission.name() + " again.");
    App.getPermissionsModule().requestIfNeeded(AskForPermissionActivity.this, mPermission);
  }

  /**
   * Displaying permission specific rationale.
   */
  private void displayRationale() {
    final ViewGroup rootView = (ViewGroup) findViewById(R.id.askForPermissionActivity);
    // Remove preexisting rationale.
    if (findViewById(R.id.rationaleFragment) != null) {
      rootView.removeView(findViewById(R.id.rationaleFragment));
    }
    // Add relevant rationale.
    LayoutInflater.from(rootView.getContext())
        .inflate(mPermission.getRationaleFragment(), rootView, true);
  }
}
