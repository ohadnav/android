package com.truethat.android.studio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Button;

import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.Scene;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.camera.CameraUtil;
import com.truethat.android.common.util.NetworkUtil;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.theater.TheaterActivity;

import java.io.IOException;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class StudioActivity extends CameraActivity {

    // Internal storage path for created scenes.
    public static final  String CREATED_SCENES_PATH = "studio/scenes/";
    // Internal storage path for created scenes.
    public static final  String SCENE_SUFFIX        = ".scene";
    // Filename for the HTTP image part.
    private static final String FILENAME = "studio-image";

    private StudioAPI mStudioAPI;
    private Callback<Scene> mSaveSceneCallback = new Callback<Scene>() {
        @Override
        public void onResponse(@NonNull Call<Scene> call, @NonNull Response<Scene> response) {
            if (response.isSuccessful()) {
                try {
                    Scene respondedScene = response.body();
                    assert respondedScene != null;
                    App.getInternalStorage()
                       .write(StudioActivity.this,
                              CREATED_SCENES_PATH + respondedScene.getId() + SCENE_SUFFIX,
                              respondedScene);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to save scene to internal storage.", e);
                } catch (NullPointerException e) {
                    Log.e(TAG, "saveScene response is missing a scene ID.");
                }
            } else {
                Log.e(TAG, "Failed to save scene.\n" + response.code() + " " + response.message() +
                        "\n" + response.headers());
            }
        }

        @Override
        public void onFailure(@NonNull Call<Scene> call, @NonNull Throwable t) {
            Log.e(TAG, "Saving scene request to " + call.request().url() + " had failed.", t);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);
        // Initialize activity transitions.
        this.overridePendingTransition(R.animator.slide_in_right,
                                       R.animator.slide_out_right);
        // Defines the navigation to the Theater.
        final ViewGroup rootView = (ViewGroup) this.findViewById(android.R.id.content);
        final Context   that     = this;
        rootView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                startActivity(new Intent(that, TheaterActivity.class));
            }
        });
        initializeStudioAPI();
        // Sets the camera preview.
        mCameraPreview = (TextureView) this.findViewById(R.id.cameraPreview);
        // Initializes capture button onClick listener.
        Button captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> {
            Log.v(TAG, "Image captured with button click.");
            takePicture();
        });
    }

    protected void processImage() {
        Log.v(TAG, "Sending multipart request to: " + StudioAPI.BASE_URL);
        MultipartBody.Part imagePart = MultipartBody.Part
                .createFormData(StudioAPI.SCENE_IMAGE_PART, FILENAME, RequestBody
                        .create(MediaType.parse("image/jpg"),
                                CameraUtil.toByteArray(supplyImage())));
        MultipartBody.Part creatorPart = MultipartBody.Part
                .createFormData(StudioAPI.DIRECTOR_PART,
                                Long.toString(App.getAuthModule().getCurrentUser().getId()));
        MultipartBody.Part timestampPart = MultipartBody.Part
                .createFormData(StudioAPI.CREATED_PART, Long.toString(new Date().getTime()));
        mStudioAPI.saveScene(imagePart, creatorPart, timestampPart)
                  .enqueue(mSaveSceneCallback);
    }

    private void initializeStudioAPI() {
        Log.v(TAG, "Initializing StudioAPI.");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(StudioAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(NetworkUtil.GSON))
                .build();

        mStudioAPI = retrofit.create(StudioAPI.class);
    }
}
