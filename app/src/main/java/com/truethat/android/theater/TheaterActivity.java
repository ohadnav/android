package com.truethat.android.theater;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.truethat.android.R;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.studio.StudioActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends CameraActivity {

    @SuppressWarnings("unused")
    protected String TAG = "TheaterActivity";

    private int         mDisplayedSceneIndex = -1;
    private List<Scene> mScenes              = new ArrayList<>();
    private ViewGroup   mRootView;
    private ProgressBar mProgressBar;

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
            public void onSwipeLeft() {
                startActivity(new Intent(that, StudioActivity.class));
            }

            @Override
            public void onSwipeUp() {
                nextAct();
            }

            @Override
            public void onSwipeDown() {
                previousAct();
            }
        });
        // Checks whether a scene was previously displayed. Usually that means the user had already
        // opened the app.
        if (mDisplayedSceneIndex >= 0) displayScene(mDisplayedSceneIndex);
        else {
            fetchScenes();
        }
    }

    private void previousAct() {
        if (mDisplayedSceneIndex <= 0) return;
        displayScene(mDisplayedSceneIndex - 1);
    }

    private void nextAct() {
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
    }
}


