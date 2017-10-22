package com.truethat.android.view.fragment;

import android.content.Context;
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
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.databinding.FragmentStudioBinding;
import com.truethat.android.model.Emotion;
import com.truethat.android.model.Media;
import com.truethat.android.viewmodel.StudioViewModel;
import com.truethat.android.viewmodel.viewinterface.StudioViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.HashMap;
import java.util.Map;

public class StudioFragment
    extends MainFragment<StudioViewInterface, StudioViewModel, FragmentStudioBinding>
    implements StudioViewInterface {
  @BindString(R.string.signing_in) String SINGING_IN;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  @BindView(R.id.parentMedia) ImageButton mPreviousButton;
  @BindView(R.id.flowLayout) ConstraintLayout mFlowLayout;
  private ImageButton mCaptureButton;
  private CameraFragment mCameraFragment;
  private MediaFragment mMediaFragment;
  private Map<Emotion, Integer> mEmotionToViewId = new HashMap<>();

  public static StudioFragment newInstance() {
    Bundle args = new Bundle();
    StudioFragment fragment = new StudioFragment();
    fragment.setArguments(args);
    return fragment;
  }

  public Map<Emotion, Integer> getEmotionToViewId() {
    return mEmotionToViewId;
  }

  @OnClick(R.id.switchCameraButton) public void switchCamera() {
    mCameraFragment.switchCamera();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.fragment_studio, getContext());
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    setUserVisibleHint(true);
  }

  @Override public void maybeChangeVisibilityState() {
    super.maybeChangeVisibilityState();
    if (mCameraFragment != null) {
      mCameraFragment.maybeChangeVisibilityState();
    }
    if (mMediaFragment != null) {
      mMediaFragment.maybeChangeVisibilityState();
    }
  }

  @Override public void onVisible() {
    super.onVisible();
    mLoadingImage.bringToFront();
    ((AnimationDrawable) mLoadingImage.getDrawable()).start();
    // Set studio toolbar icon click listeners to capture ones.
    mCaptureButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        // UI initiated picture taking.
        Log.d(TAG, "captureImage");
        // Ensures the camera is in preview state
        if (mCameraFragment.getState().equals(CameraFragment.CameraState.PREVIEW)) {
          mCameraFragment.takePicture();
        } else {
          Log.w(TAG, "Not taking a picture because camera is not in a PREVIEW state.");
        }
      }
    });
    mCaptureButton.setOnLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        // UI initiated video recording.
        Log.d(TAG, "startRecordVideo");
        if (mCameraFragment.getState().equals(CameraFragment.CameraState.PREVIEW)) {
          mCameraFragment.startRecordVideo();
          return true;
        } else {
          Log.w(TAG, "Not recording a video because camera is not in a PREVIEW state.");
          return false;
        }
      }
    });
    mCaptureButton.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        // Ensures capture touch events are made only for authorized users, and only when the camera
        // is ready.
        if (!AppContainer.getAuthManager().isAuthOk()) {
          Log.w(TAG, "Attempt to direct scene when unauthorized.");
          Toast.makeText(getContext(), SINGING_IN, Toast.LENGTH_SHORT).show();
          return true;
        }
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP
            && mCameraFragment.isRecordingVideo()) {
          stopRecordVideo();
          return true;
        }
        if (mCameraFragment.cameraNotPrepared()) {
          Log.w(TAG, "Attempt to direct scene when camera is not ready.");
          return true;
        }
        return false;
      }
    });
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Adds the camera fragment
    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    SavedState savedState = null;
    if (mCameraFragment != null
        && getFragmentManager().findFragmentById(R.id.cameraFragmentLayout) != null) {
      savedState = getFragmentManager().saveFragmentInstanceState(mCameraFragment);
    }
    mCameraFragment = CameraFragment.newInstance();
    mCameraFragment.setVisibilityListener(this);
    mCameraFragment.setCameraFragmentListener(getViewModel());
    mCameraFragment.setInitialSavedState(savedState);
    fragmentTransaction.replace(R.id.cameraFragmentLayout, mCameraFragment,
        CameraFragment.FRAGMENT_TAG);
    fragmentTransaction.commit();

    // Hooks camera preview visibility hooks.
    getViewModel().mCameraPreviewVisibility.addOnPropertyChangedCallback(
        new Observable.OnPropertyChangedCallback() {
          @Override public void onPropertyChanged(Observable sender, int propertyId) {
            getActivity().runOnUiThread(new Runnable() {
              @Override public void run() {
                if (mCameraFragment != null && mCameraFragment.getCameraPreview() != null) {
                  mCameraFragment.getCameraPreview()
                      .setVisibility(
                          getViewModel().mCameraPreviewVisibility.get() ? View.VISIBLE : View.GONE);
                }
              }
            });
          }
        });
    createFlowLayout();

    // Hooks capture button
    mCaptureButton = getActivity().findViewById(R.id.toolbar_studio);
    getViewModel().mCaptureButtonDrawableResource.addOnPropertyChangedCallback(
        new Observable.OnPropertyChangedCallback() {
          @Override public void onPropertyChanged(Observable sender, int propertyId) {
            mCaptureButton.setImageResource(getViewModel().mCaptureButtonDrawableResource.get());
          }
        });
  }

  @Override public boolean shouldBeVisible(Object o) {
    if (o instanceof MediaFragment) {
      MediaFragment mediaFragment = (MediaFragment) o;
      if (getViewModel().getCurrentMedia() == null || !getViewModel().getCurrentMedia()
          .equals(mediaFragment.getMedia())) {
        return false;
      }
    }
    return super.shouldBeVisible(o);
  }

  public MediaFragment getMediaFragment() {
    return mMediaFragment;
  }

  public void restoreCameraPreview() {
    //Restores the camera preview.
    if (mCameraFragment != null && !mCameraFragment.cameraNotPrepared()) {
      mCameraFragment.restorePreview();
    }
  }

  @Override public void displayMedia(Media media) {
    mMediaFragment = media.createFragment();
    mMediaFragment.setVisibilityListener(this);
    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.mediaContainer, mMediaFragment);
    fragmentTransaction.commit();
  }

  @Override public void removeMedia() {
    if (mMediaFragment != null) {
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
      fragmentTransaction.remove(mMediaFragment);
      fragmentTransaction.commit();
      mMediaFragment = null;
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
      ImageButton imageButton = new ImageButton(getContext());
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
