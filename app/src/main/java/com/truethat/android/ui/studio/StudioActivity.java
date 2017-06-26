package com.truethat.android.ui.studio;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioAPI;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.ui.common.BaseActivity;
import com.truethat.android.ui.common.camera.CameraFragment;
import com.truethat.android.ui.common.util.OnSwipeTouchListener;
import com.truethat.android.ui.theater.TheaterActivity;
import java.util.Date;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//TODO(ohad): add "photo approve" stage, and failure Toast when sending scene failed.
public class StudioActivity extends BaseActivity implements CameraFragment.OnPictureTakenListener {

  /**
   * File name for HTTP post request for saving scenes.
   */
  private static final String FILENAME = "studio-image";
  @VisibleForTesting @BindString(R.string.signing_in) String UNAUTHORIZED_TOAST = "Signing in...";
  /**
   * API interface for saving scenes.
   */
  private StudioAPI mStudioAPI = NetworkUtil.createAPI(StudioAPI.class);
  private Callback<ResponseBody> mSaveSceneCallback = new Callback<ResponseBody>() {
    @Override public void onResponse(@NonNull Call<ResponseBody> call,
        @NonNull Response<ResponseBody> response) {
      if (response.isSuccessful()) {
        // Navigate to theater after posting.
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      } else {
        Log.e(TAG,
            "Failed to save scene.\n" + response.code() + " " + response.message() + "\n" + response
                .headers());
      }
    }

    @Override public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
      Log.e(TAG, "Saving scene request to " + call.request().url() + " had failed.", t);
    }
  };
  private CameraFragment mCameraFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Initialize activity transitions.
    this.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_right);
    // Defines the navigation to the Theater.
    mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeDown() {
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      }
    });
    // Hooks the camera fragment
    mCameraFragment =
        (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_studio;
  }

  /**
   * UI initiated picture taking.
   */
  @OnClick(R.id.captureButton) public void captureImage() {
    if (App.getAuthModule().isAuthOk()) {
      mCameraFragment.takePicture();
    } else {
      Toast.makeText(this, UNAUTHORIZED_TOAST, Toast.LENGTH_SHORT).show();
    }
  }

  @Override public void processImage(Image image) {
    Log.v(TAG, "Sending multipart request to: " + NetworkUtil.getBackendUrl());
    MultipartBody.Part imagePart =
        MultipartBody.Part.createFormData(StudioAPI.SCENE_IMAGE_PART, FILENAME,
            RequestBody.create(MediaType.parse("image/jpg"), CameraUtil.toByteArray(image)));
    MultipartBody.Part creatorPart = MultipartBody.Part.createFormData(StudioAPI.DIRECTOR_PART,
        NetworkUtil.GSON.toJson(App.getAuthModule().getUser()));
    MultipartBody.Part timestampPart = MultipartBody.Part.createFormData(StudioAPI.CREATED_PART,
        Long.toString(new Date().getTime()));
    mStudioAPI.saveScene(imagePart, creatorPart, timestampPart).enqueue(mSaveSceneCallback);
  }
}
