package com.truethat.android.studio;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.truethat.android.R;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.identity.User;
import com.truethat.android.theater.TheaterActivity;

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

    private static final String FILENAME = "studio-image";

    private Callback<Long> saveSceneCallback = new Callback<Long>() {
        @Override
        public void onResponse(@NonNull Call<Long> call, @NonNull Response<Long> response) {
            if (response.isSuccessful()) {
                Toast.makeText(StudioActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("UploadCallback",
                      "Code: " + response.code() + " Message: " + response
                              .message() + "\nHeaders:\n" + response.headers());
                Toast.makeText(StudioActivity.this, "no Upload", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(@NonNull Call<Long> call, @NonNull Throwable t) {

        }
    };
    private StudioAPI mStudioAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = this.getClass().getSimpleName();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);
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

    @Override
    protected void processImage(Image image) {
        Log.v(TAG, "Sending multipart request to: " + StudioAPI.BASE_URL);
        MultipartBody.Part imagePart = MultipartBody.Part
                .createFormData(StudioAPI.SCENE_IMAGE_PART, FILENAME, RequestBody
                        .create(MediaType.parse("image/jpg"),
                                image.getPlanes()[0].getBuffer().array()));

        mStudioAPI.saveScene(imagePart, User.ID, new Date()).enqueue(saveSceneCallback);
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
