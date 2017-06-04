package com.truethat.android.theater;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.truethat.android.R;
import com.truethat.android.common.Scene;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.util.NetworkUtil;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.studio.StudioActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends CameraActivity {
    private int         mDisplayedSceneIndex = -1;
    private List<Scene> mScenes              = new ArrayList<>();
    private ViewGroup   mRootView;
    private ProgressBar mProgressBar;

    private TheaterAPI mTheaterAPI;
    private Callback<List<Scene>> mGetScenesCallback = new Callback<List<Scene>>() {
        @Override
        public void onResponse(@NonNull Call<List<Scene>> call,
                               @NonNull Response<List<Scene>> response) {
            if (response.isSuccessful()) {
                int         toDisplayIndex = mScenes.size();
                List<Scene> newScenes      = response.body();
                assert newScenes != null;
                mScenes.addAll(newScenes);
                if (!newScenes.isEmpty()) {
                    displayScene(toDisplayIndex);
                }
            } else {
                Log.e(TAG, "Failed to fetch scenes from " + call.request().url() + "\n" +
                        response.code() + " " + response.message() + "\n" + response.headers());
            }
        }

        @Override
        public void onFailure(@NonNull Call<List<Scene>> call, @NonNull Throwable t) {
            Log.e(TAG, "Fetch scenes request to " + call.request().url() + " had failed.", t);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theater);
        // Animation for screen transitions.
        this.overridePendingTransition(R.animator.slide_in_left,
                                       R.animator.slide_out_left);
        // Initializes UI elements.
        mProgressBar = (ProgressBar) this.findViewById(R.id.progressBar);
        // Hooks for screen swipes
        mRootView = (ViewGroup) this.findViewById(android.R.id.content);
        final Context that = this;
        mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                startActivity(new Intent(that, StudioActivity.class));
            }

            @Override
            public void onSwipeDown() {
                nextAct();
            }

            @Override
            public void onSwipeUp() {
                previousAct();
            }
        });
        // Initializes the Theater API
        initializeTheaterAPI();
        // Checks whether a scene was previously displayed. Usually that means the user had already
        // opened the app.
        if (mDisplayedSceneIndex >= 0) displayScene(mDisplayedSceneIndex);
        else {
            fetchScenes();
        }
    }

    private void previousAct() {
        Log.v(TAG, "Previous act");
        if (mDisplayedSceneIndex <= 0) return;
        displayScene(mDisplayedSceneIndex - 1);
    }

    private void nextAct() {
        Log.v(TAG, "Next act");
        if (mDisplayedSceneIndex >= mScenes.size() - 1) {
            fetchScenes();
            return;
        }
        displayScene(mDisplayedSceneIndex + 1);
    }

    private void displayScene(int displayIndex) {
        if (displayIndex < 0 || displayIndex >= mScenes.size()) {
            IndexOutOfBoundsException e = new IndexOutOfBoundsException();
            Log.e(TAG, displayIndex + " is not a scene index.", e);
            throw e;
        }
        Log.v(TAG, "Displaying scene " + displayIndex);
        // Hides default image.
        ImageView imageView = (ImageView) this.findViewById(R.id.defaultImage);
        imageView.setVisibility(View.GONE);
        // Stores the new displayed scene index.
        mDisplayedSceneIndex = displayIndex;
        // Displaying the new scene.
        Scene       displayedScene = mScenes.get(displayIndex);
        SceneLayout sceneLayout    = new SceneLayout(displayedScene, this);
        mRootView.addView(sceneLayout);
        // Hides loading animation.
        mProgressBar.setVisibility(View.GONE);
    }

    private void fetchScenes() {
        Log.v(TAG, "Fetching scenes...");
        mProgressBar.setVisibility(View.VISIBLE);
        mTheaterAPI.getScenes().enqueue(mGetScenesCallback);
    }

    private void initializeTheaterAPI() {
        Log.v(TAG, "Initializing TheaterAPI.");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TheaterAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(NetworkUtil.GSON))
                .build();

        mTheaterAPI = retrofit.create(TheaterAPI.class);
    }
}


