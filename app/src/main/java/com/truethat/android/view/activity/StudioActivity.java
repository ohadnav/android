package com.truethat.android.view.activity;

import android.content.Intent;
import android.databinding.Observable;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
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
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Media;
import com.truethat.android.view.custom.OnSwipeTouchListener;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.view.fragment.MediaFragment;
import com.truethat.android.viewmodel.StudioViewModel;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class StudioActivity
    extends BaseActivity<StudioViewInterface, StudioViewModel, ActivityStudioBinding>
    implements StudioViewInterface {
  public static final String DIRECTED_SCENE_TAG = "DIRECTED_SCENE_TAG";
  @BindString(R.string.signing_in) String SINGING_IN;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  @BindView(R.id.captureButton) ImageButton mCaptureButton;
  @BindView(R.id.parentMedia) ImageButton mPreviousButton;
  @BindView(R.id.flowLayout) ConstraintLayout mFlowLayout;
  private CameraFragment mCameraFragment;
  private Map<Emotion, Integer> mEmotionToViewId = new HashMap<>();

  public Map<Emotion, Integer> getEmotionToViewId() {
    return mEmotionToViewId;
  }

  /**
   * UI initiated picture taking.
   */
  @OnClick(R.id.captureButton) public void captureImage() {
    Log.d(TAG, "captureImage");
    // Ensures the camera is in preview state
    if (mCameraFragment.getState().equals(CameraFragment.CameraState.PREVIEW)) {
      mCameraFragment.takePicture();
    } else {
      Log.w(TAG, "Not taking a picture because camera is not in a PREVIEW state.");
    }
  }

  /**
   * UI initiated video recording.
   */
  @OnLongClick(R.id.captureButton) public boolean startRecordVideo() {
    Log.d(TAG, "startRecordVideo");
    if (mCameraFragment.getState().equals(CameraFragment.CameraState.PREVIEW)) {
      mCameraFragment.startRecordVideo();
      return true;
    } else {
      Log.w(TAG, "Not recording a video because camera is not in a PREVIEW state.");
      return false;
    }
  }

  /**
   * Ensures capture touch events are made only for authorized users, and only when the camera is
   * ready.
   */
  @OnTouch(R.id.captureButton) public boolean onCaptureTouch(MotionEvent motionEvent) {
    if (!AppContainer.getAuthManager().isAuthOk()) {
      Log.w(TAG, "Attempt to direct scene when unauthorized.");
      Toast.makeText(StudioActivity.this, SINGING_IN, Toast.LENGTH_SHORT).show();
      onAuthFailed();
      return true;
    }
    if (motionEvent.getAction() == MotionEvent.ACTION_UP && mCameraFragment.isRecordingVideo()) {
      stopRecordVideo();
      return true;
    }
    if (mCameraFragment.cameraNotPrepared()) {
      Log.w(TAG, "Attempt to direct scene when camera is not ready.");
      return true;
    }
    return false;
  }

  @OnClick(R.id.switchCameraButton) public void switchCamera() {
    mCameraFragment.switchCamera();
  }

  @Override public void onStart() {
    super.onStart();
    mLoadingImage.bringToFront();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
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
    createFlowLayout();
  }

  public void leaveStudio() {
    runOnUiThread(new Runnable() {
      @Override public void run() {
        // Removes display fragment
        if (getSupportFragmentManager().findFragmentByTag(DIRECTED_SCENE_TAG) != null) {
          FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
          fragmentTransaction.remove(
              getSupportFragmentManager().findFragmentByTag(DIRECTED_SCENE_TAG));
          fragmentTransaction.commit();
        }
        // Navigate to theater after publishing.
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      }
    });
  }

  public void restoreCameraPreview() {
    //Restores the camera preview.
    mCameraFragment.restorePreview();
  }

  @Override public void displayPreview(Media media) {
    MediaFragment mediaFragment = media.createFragment();
    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.mediaContainer, mediaFragment, DIRECTED_SCENE_TAG);
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

  /**
   * Stop video recording.
   */
  private void stopRecordVideo() {
    Log.d(TAG, "stopRecordVideo");
    mCameraFragment.stopRecordVideo();
  }

  private void createFlowLayout() {
    // Create array of view IDs of emoji image buttons and previous media button
    int[] viewIds = new int[Emotion.values().length + 1];
    for (final Emotion emotion : Emotion.values()) {
      // Generates a unique ID for each reaction.
      int viewId = View.generateViewId();
      ImageButton imageButton = new ImageButton(this);
      imageButton.setId(viewId);
      imageButton.setImageResource(emotion.getDrawableResource());
      imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
      // Transparent background
      imageButton.getBackground().setAlpha(0);
      imageButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          getViewModel().onReactionChosen(emotion);
        }
      });
      mEmotionToViewId.put(emotion, viewId);
      // Set the image button size to 55 x 55
      ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
          (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55,
              getResources().getDisplayMetrics()),
          (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 55,
              getResources().getDisplayMetrics()));
      mFlowLayout.addView(imageButton, layoutParams);
      viewIds[emotion.ordinal()] = viewId;
      // Set each button to be centered horizontally.
      ConstraintSet constraintSet = new ConstraintSet();
      constraintSet.clone(mFlowLayout);
      constraintSet.connect(viewId, ConstraintSet.START, R.id.flowLayout, ConstraintSet.START, 0);
      constraintSet.connect(viewId, ConstraintSet.END, R.id.flowLayout, ConstraintSet.END, 0);
      constraintSet.applyTo(mFlowLayout);
    }
    mFlowLayout.bringToFront();
    viewIds[Emotion.values().length] = R.id.parentMedia;
    // Create a chain of emotions, so that they do not overlap one another.
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(mFlowLayout);
    constraintSet.createVerticalChain(R.id.flowLayout, ConstraintSet.TOP, R.id.flowLayout,
        ConstraintSet.BOTTOM, viewIds, null, ConstraintSet.CHAIN_SPREAD);
    constraintSet.applyTo(mFlowLayout);
  }
}
