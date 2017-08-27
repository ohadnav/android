package com.truethat.android.view.activity;

import android.content.Intent;
import android.databinding.Observable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.databinding.ActivityStudioBinding;
import com.truethat.android.databinding.FragmentReactableBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.custom.OnSwipeTouchListener;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.view.fragment.ReactableFragment;
import com.truethat.android.viewmodel.ReactableViewModel;
import com.truethat.android.viewmodel.StudioViewModel;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class StudioActivity
    extends BaseActivity<StudioViewInterface, StudioViewModel, ActivityStudioBinding>
    implements StudioViewInterface {
  public static final String DIRECTED_REACTABLE_TAG = "DIRECTED_REACTABLE_TAG";
  @VisibleForTesting @BindString(R.string.signing_in) String UNAUTHORIZED_TOAST;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  @BindView(R.id.captureButton) ImageButton mCaptureButton;
  private CameraFragment mCameraFragment;

  /**
   * UI initiated picture taking.
   */
  @OnClick(R.id.captureButton) public void captureImage() {
    Log.v(TAG, "captureImage");
    mCameraFragment.takePicture();
  }

  /**
   * UI initiated video recording.
   */
  @OnLongClick(R.id.captureButton) public boolean startRecordVideo() {
    Log.v(TAG, "startRecordVideo");
    mCameraFragment.startRecordVideo();
    return true;
  }

  /**
   * Ensures capture touch events are made only for authorized users, and only when the camera is
   * ready.
   */
  @OnTouch(R.id.captureButton) public boolean onCaptureTouch(MotionEvent motionEvent) {
    if (!AppContainer.getAuthManager().isAuthOk()) {
      Log.w(TAG, "Attempt to direct reactable when unauthorized.");
      Toast.makeText(StudioActivity.this, UNAUTHORIZED_TOAST, Toast.LENGTH_SHORT).show();
      onAuthFailed();
      return true;
    }
    if (motionEvent.getAction() == MotionEvent.ACTION_UP && mCameraFragment.isRecordingVideo()) {
      stopRecordVideo();
      return true;
    }
    if (!mCameraFragment.canUseCamera()) {
      Log.w(TAG, "Attempt to direct reactable when camera is not ready.");
      return true;
    }
    return false;
  }

  /**
   * Stop video recording.
   */
  public void stopRecordVideo() {
    Log.v(TAG, "stopRecordVideo");
    mCameraFragment.stopRecordVideo();
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
    mCameraFragment.setCameraFragmentListener(getViewModel());

    // Hooks camera preview visibility hooks.
    getViewModel().mCameraPreviewVisibility.addOnPropertyChangedCallback(
        new Observable.OnPropertyChangedCallback() {
          @Override public void onPropertyChanged(Observable sender, int propertyId) {
            runOnUiThread(new Runnable() {
              @Override public void run() {
                mCameraFragment.getCameraPreview()
                    .setVisibility(
                        getViewModel().mCameraPreviewVisibility.get() ? View.VISIBLE : View.GONE);
              }
            });
          }
        });
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

  @Override public void displayPreview(Reactable reactable) {
    Log.e(TAG, "displayPreview");
    @SuppressWarnings("unchecked")
    ReactableFragment<Reactable, ReactableViewModel<Reactable>, FragmentReactableBinding>
        directedReactableFragment = reactable.createFragment();
    directedReactableFragment.displayOnly();
    Log.e(TAG, "display only");
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.previewLayout, directedReactableFragment,
        DIRECTED_REACTABLE_TAG);
    fragmentTransaction.commit();

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
