package com.truethat.android.ui.theater;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.TheaterAPI;
import com.truethat.android.model.Reactable;
import com.truethat.android.ui.common.BaseActivity;
import com.truethat.android.ui.common.camera.CameraFragment;
import com.truethat.android.ui.common.media.ReactableFragment;
import com.truethat.android.ui.common.media.ReactableFragmentAdapter;
import com.truethat.android.ui.common.util.OnSwipeTouchListener;
import com.truethat.android.ui.studio.StudioActivity;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

/**
 * Theater is where users interact with scenes.
 */
public class TheaterActivity extends BaseActivity
    implements ReactableFragment.OnReactableInteractionListener,
    CameraFragment.OnPictureTakenListener {
  @BindView(R.id.reactablesPager) ViewPager mPager;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  @BindView(R.id.loadingLayout) ViewGroup mLoadingLayout;
  @BindView(R.id.notFoundText) TextView mNotFoundText;
  private ReactableFragmentAdapter mReactableFragmentAdapter;
  private TheaterAPI mTheaterAPI;
  private Callback<List<Reactable>> mFetchReactablesCallback;
  private Call<List<Reactable>> mFetchReactablesCall;
  private CameraFragment mCameraFragment;

  @Override public void onAuthOk() {
    if (mPager.getAdapter().getCount() == 0) fetchReactables();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Animation for screen transitions.
    this.overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left);
    // Navigation to other activities
    mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeLeft() {
        if (mPager.getAdapter().getCount() == 0) {
          fetchReactables();
        }
      }

      @Override public void onSwipeUp() {
        startActivity(new Intent(TheaterActivity.this, StudioActivity.class));
      }
    });
    // Initialize views
    mReactableFragmentAdapter = new ReactableFragmentAdapter(getSupportFragmentManager());
    mPager.setAdapter(mReactableFragmentAdapter);
    mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_DRAGGING
            && mPager.getCurrentItem() == mPager.getAdapter().getCount() - 1) {
          fetchReactables();
        }
      }
    });
    // Hooks the camera fragment
    mCameraFragment =
        (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);
    // Initializes the Theater API
    mTheaterAPI = NetworkUtil.createAPI(TheaterAPI.class);
    mFetchReactablesCallback = buildFetchReactablesCallback();
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_theater;
  }

  @Override public void processImage(Image image) {
    // Pushes new input to the detection module.
    App.getReactionDetectionModule().attempt(image);
  }

  @Override public void requestDetectionInput() {
    mCameraFragment.takePicture();
  }

  /**
   * Fetching {@link Reactable} from our backend.
   */
  public void fetchReactables() {
    Log.v(TAG, "Fetching reactables...");
    if (mPager.getAdapter().getCount() == 0) {
      mLoadingLayout.setVisibility(View.VISIBLE);
      mNotFoundText.setVisibility(GONE);
      Glide.with(this).load(R.drawable.anim_loading_elephant).into(mLoadingImage);
    }
    mFetchReactablesCall = mTheaterAPI.getReactables();
    mFetchReactablesCall.enqueue(mFetchReactablesCallback);
  }

  @Override protected void onPause() {
    super.onPause();
    if (mFetchReactablesCall != null) mFetchReactablesCall.cancel();
  }

  @OnClick(R.id.notFoundText) void navigateToStudio() {
    startActivity(new Intent(TheaterActivity.this, StudioActivity.class));
  }

  /**
   * @return fetching callback. If new {@link Reactable}s are retrieved, then they are added to
   * {@link #mPager}.
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
            Log.v(TAG, "Loading " + newReactables.size() + " new reactables.");
            // Hides the loading layout.
            mLoadingLayout.setVisibility(GONE);
            // Display new reactables.
            int toDisplayIndex = mPager.getAdapter().getCount();
            mReactableFragmentAdapter.add(newReactables);
            mPager.setCurrentItem(toDisplayIndex);
          } else if (mPager.getAdapter().getCount() == 0) {
            TheaterActivity.this.runOnUiThread(new Runnable() {
              @Override public void run() {
                displayNotFound();
              }
            });
          }
        } else {
          Log.e(TAG, "Failed to get reactables from "
              + call.request().url()
              + "\n"
              + response.code()
              + " "
              + response.message()
              + "\n"
              + response.headers());
          TheaterActivity.this.runOnUiThread(new Runnable() {
            @Override public void run() {
              displayNotFound();
            }
          });
        }
      }

      @Override public void onFailure(@NonNull Call<List<Reactable>> call, @NonNull Throwable t) {
        Log.e(TAG, "Fetch reactables request to " + call.request().url() + " had failed.", t);
        TheaterActivity.this.runOnUiThread(new Runnable() {
          @Override public void run() {
            displayNotFound();
          }
        });
      }
    };
  }

  @MainThread private void displayNotFound() {
    // Shows not found text
    mLoadingLayout.setVisibility(View.VISIBLE);
    mNotFoundText.setVisibility(View.VISIBLE);
    Glide.with(TheaterActivity.this).load(R.drawable.sad_teddy).into(mLoadingImage);
  }
}


