package com.truethat.android.studio;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.camera.CameraUtil;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.theater.Scene;
import com.truethat.android.theater.TheaterActivity;

import java.io.IOException;

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
    // Current/last processed scene.
    private Scene mScene;

    private StudioAPI mStudioAPI;
    private Callback<Long> saveSceneCallback = new Callback<Long>() {
        @Override
        public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
            if (response.isSuccessful()) {
                try {
                    // Appends the scene ID, as returned from the response.
                    //noinspection ConstantConditions
                    mScene.setId(response.body());
                    App.getInternalStorage()
                       .write(StudioActivity.this,
                              CREATED_SCENES_PATH + mScene.getId() + SCENE_SUFFIX, mScene);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to save scene to internal storage.", e);
                } catch (NullPointerException e) {
                    Log.e(TAG, "Save scene response is missing a scene ID.");
                }
            } else {
                Log.e(TAG, "Code: " + response.code() + " Message: " + response
                        .message() + "\nHeaders:\n" + response.headers());
            }
        }

        @Override
        public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {
            Log.e(TAG, "Saving scene failed.", t);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Sets TAG to subclass name.
        TAG = this.getClass().getSimpleName();
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
            public void onSwipeRight() {
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
        mScene = new Scene(CameraUtil.toByteArray(supplyImage()));
        MultipartBody.Part imagePart = MultipartBody.Part
                .createFormData(StudioAPI.SCENE_IMAGE_PART, FILENAME, RequestBody
                        .create(MediaType.parse("image/jpg"), mScene.getImageBytes()));
        MultipartBody.Part creatorPart = MultipartBody.Part
                .createFormData(StudioAPI.CREATOR_PART, Long.toString(mScene.getCreator().getId()));
        MultipartBody.Part timestampPart = MultipartBody.Part
                .createFormData(StudioAPI.TIMESTAMP_PART, mScene.getTimestamp().toString());
        mStudioAPI.saveScene(imagePart, creatorPart, timestampPart)
                  .enqueue(saveSceneCallback);
    }

    private void initializeStudioAPI() {
        Log.v(TAG, "Initializing StudioAPI.");
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(StudioAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        mStudioAPI = retrofit.create(StudioAPI.class);
    }
}
