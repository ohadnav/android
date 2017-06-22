package com.truethat.android.studio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.camera.CameraUtil;
import com.truethat.android.common.media.Scene;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.theater.TheaterActivity;
import java.util.Date;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudioActivity extends CameraActivity {

  @VisibleForTesting static final String UNAUTHORIZED_TOAST = "Signing in...";
  /**
   * File name for HTTP post request for saving scenes.
   */
  private static final String FILENAME = "studio-image";
  /**
   * Retrofit API interface for saving scenes.
   */
  private StudioAPI mStudioAPI = NetworkUtil.createAPI(StudioAPI.class);
  private Callback<Scene> mSaveSceneCallback = new Callback<Scene>() {
    @Override public void onResponse(@NonNull Call<Scene> call, @NonNull Response<Scene> response) {
      if (!response.isSuccessful()) {
        Log.e(TAG, "Bad response for saving scene.\n"
            + response.code()
            + " "
            + response.message()
            + "\n"
            + response
                .headers());
      }
    }

    @Override public void onFailure(@NonNull Call<Scene> call, @NonNull Throwable t) {
      Log.e(TAG, "Saving scene request to " + call.request().url() + " had failed.", t);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_studio);
    // Initialize activity transitions.
    this.overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_right);
    // Defines the navigation to the Theater.
    final ViewGroup rootView = (ViewGroup) this.findViewById(R.id.studioActivity);
    rootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeDown() {
        startActivity(new Intent(StudioActivity.this, TheaterActivity.class));
      }
    });
    // Sets the camera preview.
    mCameraPreview = (TextureView) this.findViewById(R.id.cameraPreview);
  }

  /**
   * UI initiated picture taking.
   */
  public void takePicture(View view) {
    if (App.getAuthModule().isAuthOk()) {
      takePicture();
    } else {
      Toast.makeText(this, UNAUTHORIZED_TOAST, Toast.LENGTH_SHORT).show();
    }
  }

  protected void processImage() {
    if (App.getAuthModule().isAuthOk()) {
      sendImage();
    } else {
      Toast.makeText(this, UNAUTHORIZED_TOAST, Toast.LENGTH_SHORT).show();
    }
  }

  private void sendImage() {
    MultipartBody.Part imagePart =
        MultipartBody.Part.createFormData(StudioAPI.SCENE_IMAGE_PART, FILENAME,
            RequestBody.create(MediaType.parse("image/jpg"),
                CameraUtil.toByteArray(supplyImage())));
    MultipartBody.Part creatorPart = MultipartBody.Part.createFormData(StudioAPI.DIRECTOR_PART,
        Long.toString(App.getAuthModule().getUser().getId()));
    MultipartBody.Part timestampPart = MultipartBody.Part.createFormData(StudioAPI.CREATED_PART,
        Long.toString(new Date().getTime()));
    mStudioAPI.saveScene(imagePart, creatorPart, timestampPart).enqueue(mSaveSceneCallback);
  }
}
