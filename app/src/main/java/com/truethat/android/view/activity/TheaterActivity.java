package com.truethat.android.view.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.truethat.android.R;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.databinding.ActivityTheaterBinding;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.fragment.CameraFragment;
import com.truethat.android.view.fragment.ReactableFragment;
import com.truethat.android.viewmodel.TheaterViewModel;
import com.truethat.android.viewmodel.viewinterface.TheaterViewInterface;
import eu.inloop.viewmodel.binding.ViewModelBindingConfig;
import java.util.List;
import retrofit2.Call;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity
    extends ReactablesPagerActivity<TheaterViewInterface, TheaterViewModel, ActivityTheaterBinding>
    implements ReactableFragment.ReactionDetectionListener,
    CameraFragment.OnPictureTakenListener {
  private TheaterApi mTheaterApi;
  private CameraFragment mCameraFragment;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Animation for screen transitions.
    this.overridePendingTransition(R.animator.slide_in_bottom, R.animator.slide_out_bottom);
    // Hooks the camera fragment
    mCameraFragment =
        (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
    // Initializes the Theater API
    mTheaterApi = createApiInterface(TheaterApi.class);
  }

  @Override protected Call<List<Reactable>> buildFetchReactablesCall() {
    return mTheaterApi.fetchReactables(mAuthManager.currentUser());
  }

  @Override protected void onSwipeUp() {
    startActivity(new Intent(TheaterActivity.this, StudioActivity.class));
  }

  @Override public void processImage(Image image) {
    // Pushes new input to the detection module.
    mReactionDetectionManager.attempt(image);
  }

  @Override public void requestDetectionInput() {
    mCameraFragment.takePicture();
  }

  @Nullable @Override public ViewModelBindingConfig getViewModelBindingConfig() {
    return new ViewModelBindingConfig(R.layout.activity_theater, this);
  }
}


