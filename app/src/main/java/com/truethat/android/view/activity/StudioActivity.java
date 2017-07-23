package com.truethat.android.view.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.databinding.ActivityStudioBinding;
import com.truethat.android.model.Scene;
import com.truethat.android.view.custom.OnSwipeTouchListener;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.viewmodel.StudioViewModel;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class StudioActivity
    extends BaseActivity<StudioViewInterface, StudioViewModel, ActivityStudioBinding>
    implements CameraFragment.OnPictureTakenListener, StudioViewInterface {

  @VisibleForTesting @BindString(R.string.signing_in) String UNAUTHORIZED_TOAST;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  private CameraFragment mCameraFragment;

  /**
   * UI initiated picture taking.
   */
  @OnClick(R.id.captureButton) public void captureImage() {
    if (mAuthManager.isAuthOk() && mCameraFragment.isCameraOpen()) {
      mCameraFragment.takePicture();
    } else {
      Toast.makeText(this, UNAUTHORIZED_TOAST, Toast.LENGTH_SHORT).show();
    }
  }

  @Override public void processImage(Image image) {
    getViewModel().onApproval(new Scene(mAuthManager.currentUser(), CameraUtil.toByteArray(image)));
  }

  @OnClick(R.id.switchCameraButton) public void switchCamera() {
    mCameraFragment.switchCamera();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_studio, this);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Defines the navigation to the Theater.
    mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeUp() {
        Intent intent = new Intent(StudioActivity.this, RepertoireActivity.class);
        intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
      }

      @Override public void onSwipeDown() {
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      }
    });
    // Hooks the camera fragment
    mCameraFragment =
        (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
  }

  public void onPublished() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        // Navigate to theater after publishing.
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      }
    });
  }

  public void onApproval() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        // Removes preview tint
        mCameraFragment.getCameraPreview().setBackgroundTintList(null);
      }
    });
  }

  public void onSent() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        // Tinting camera preview and showing a loader.
        mCameraFragment.getCameraPreview().setBackgroundTintList(getColorStateList(R.color.tint));
        mLoadingImage.bringToFront();
      }
    });
  }

  public void onDirecting() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        //Restores the camera preview.
        mCameraFragment.restorePreview();
        // Removes preview tint
        mCameraFragment.getCameraPreview().setBackgroundTintList(null);
      }
    });
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    // Initialize activity transitions.
    if (intent.getBooleanExtra(RepertoireActivity.FROM_REPERTOIRE, false)) {
      this.overridePendingTransition(R.animator.slide_in_bottom, R.animator.slide_out_bottom);
    } else {
      this.overridePendingTransition(R.animator.slide_in_top, R.animator.slide_out_top);
    }
  }
}
