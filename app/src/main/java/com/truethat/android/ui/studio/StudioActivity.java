package com.truethat.android.ui.studio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.view.TextureView;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.OnClick;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioAPI;
import com.truethat.android.common.util.CameraUtil;
import com.truethat.android.model.Scene;
import com.truethat.android.ui.common.camera.CameraActivity;
import com.truethat.android.ui.common.util.OnSwipeTouchListener;
import com.truethat.android.ui.theater.TheaterActivity;
import java.io.IOException;
import java.util.Date;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudioActivity extends CameraActivity {

  /**
   * File name for HTTP post request for saving scenes.
   */
  private static final String FILENAME = "studio-image";
  @VisibleForTesting @BindString(R.string.signing_in) String UNAUTHORIZED_TOAST = "Signing in...";
  /**
   * Retrofit API interface for saving scenes.
   */
  private StudioAPI mStudioAPI = NetworkUtil.createAPI(StudioAPI.class);
  private Callback<Scene> mSaveSceneCallback = new Callback<Scene>() {
    @Override public void onResponse(@NonNull Call<Scene> call, @NonNull Response<Scene> response) {
      if (response.isSuccessful()) {
        try {
          Scene respondedScene = response.body();
          if (respondedScene == null) {
            throw new AssertionError("Responded scene no tiene nada!");
          }
          App.getInternalStorage()
              .write(StudioActivity.this, respondedScene.internalStoragePath(), respondedScene);
        } catch (IOException e) {
          Log.e(TAG, "Failed to save scene to internal storage.", e);
        } catch (NullPointerException e) {
          Log.e(TAG, "saveScene response is null.");
        }
      } else {
        Log.e(TAG,
            "Failed to save scene.\n" + response.code() + " " + response.message() + "\n" + response
                .headers());
      }
    }

    @Override public void onFailure(@NonNull Call<Scene> call, @NonNull Throwable t) {
      Log.e(TAG, "Saving scene request to " + call.request().url() + " had failed.", t);
    }
  };

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
    // Sets the camera preview.
    mCameraPreview = (TextureView) this.findViewById(R.id.cameraPreview);
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_studio;
  }

  /**
   * UI initiated picture taking.
   */
  @OnClick(R.id.captureButton) public void captureImage() {
    if (App.getAuthModule().isAuthOk()) {
      takePicture();
    } else {
      Toast.makeText(this, UNAUTHORIZED_TOAST, Toast.LENGTH_SHORT).show();
    }
  }

  protected void processImage() {
    Log.v(TAG, "Sending multipart request to: " + NetworkUtil.getBackendUrl());
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
