package com.truethat.android.ui.activity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import com.truethat.android.R;
import com.truethat.android.common.network.TheaterApi;
import com.truethat.android.model.Reactable;
import com.truethat.android.ui.common.camera.CameraFragment;
import com.truethat.android.ui.common.media.ReactableFragment;
import java.util.List;
import retrofit2.Call;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends ReactablesPagerActivity
    implements ReactableFragment.ReactionDetectionListener,
    CameraFragment.OnPictureTakenListener {
  private TheaterApi mTheaterApi;
  private CameraFragment mCameraFragment;

  @Override public void onAuthOk() {
    if (mPager.getAdapter().getCount() == 0) fetchReactables();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
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

  @Override protected int getLayoutResId() {
    return R.layout.activity_theater;
  }
}


