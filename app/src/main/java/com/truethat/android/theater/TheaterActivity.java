package com.truethat.android.theater;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.camera.CameraActivity;
import com.truethat.android.common.media.Reactable;
import com.truethat.android.common.media.ReactableFragment;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.util.OnSwipeTouchListener;
import com.truethat.android.studio.StudioActivity;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends CameraActivity
    implements ReactableFragment.OnReactableInteractionListener {
  private ReactableFragmentAdapter mReactableFragmentAdapter;
  private ViewPager mPager;
  private TheaterAPI mTheaterAPI;
  private Callback<List<Reactable>> mFetchReactablesCallback;
  private Call<List<Reactable>> mFetchReactablesCall;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_theater);
    // Animation for screen transitions.
    this.overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left);
    // Navigation to other activities
    findViewById(R.id.theaterActivity).setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeUp() {
        startActivity(new Intent(TheaterActivity.this, StudioActivity.class));
      }
    });
    // Displays loading gif
    Glide.with(this)
        .load(R.drawable.loading_elephant)
        .asGif()
        .fitCenter()
        .into((ImageView) findViewById(R.id.loadingImage));
    // Initialize views
    mReactableFragmentAdapter = new ReactableFragmentAdapter(getSupportFragmentManager());
    mPager = (ViewPager) findViewById(R.id.reactablesPager);
    mPager.setAdapter(mReactableFragmentAdapter);
    mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING
            && mPager.getCurrentItem() == mPager.getAdapter().getCount() - 1) {
          fetchReactables();
        }
      }
    });
    // Initializes the Theater API
    mTheaterAPI = NetworkUtil.createAPI(TheaterAPI.class);
    mFetchReactablesCallback = buildFetchReactablesCallback();
  }

  @Override protected void processImage() {
    // Pushes new input to the detection module.
    App.getReactionDetectionModule().attempt(supplyImage());
  }

  @Override public void onAuthOk() {
    if (mPager.getAdapter().getCount() == 0) fetchReactables();
  }

  @Override public void requestDetectionInput() {
    takePicture();
  }

  @Override protected void onPause() {
    super.onPause();
    if (mFetchReactablesCall != null) mFetchReactablesCall.cancel();
  }

  /**
   * Fetching {@link Reactable} from our backend.
   */
  public void fetchReactables() {
    Log.v(TAG, "Fetching reactables...");
    mFetchReactablesCall = mTheaterAPI.getReactables();
    mFetchReactablesCall.enqueue(mFetchReactablesCallback);
  }

  /**
   * @return fetching callback. If new {@link Reactable}s are retrieved, then they are added to {@link #mPager}.
   */
  private Callback<List<Reactable>> buildFetchReactablesCallback() {
    return new Callback<List<Reactable>>() {
      @Override public void onResponse(@NonNull Call<List<Reactable>> call,
          @NonNull Response<List<Reactable>> response) {
        if (response.isSuccessful()) {
          List<Reactable> newReactables = response.body();
          if (newReactables == null) {
            throw new AssertionError("I just cant believe it! The new reactables are null.");
          }
          if (newReactables.size() > 0) {
            // Hide loading image.
            findViewById(R.id.loadingImage).setVisibility(GONE);
            // Display new reactables.
            int toDisplayIndex = mPager.getAdapter().getCount();
            mReactableFragmentAdapter.add(newReactables);
            mPager.setCurrentItem(toDisplayIndex);
          }
        } else {
          Log.e(TAG, "Failed to get scenes from "
              + call.request().url()
              + "\n"
              + response.code()
              + " "
              + response.message()
              + "\n"
              + response.headers());
        }
      }

      @Override public void onFailure(@NonNull Call<List<Reactable>> call, @NonNull Throwable t) {
        Log.e(TAG, "Fetch scenes request to " + call.request().url() + " had failed.", t);
      }
    };
  }
}


