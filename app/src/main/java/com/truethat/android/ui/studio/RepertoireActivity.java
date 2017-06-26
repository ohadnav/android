package com.truethat.android.ui.studio;

import android.content.Intent;
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
import com.bumptech.glide.Glide;
import com.truethat.android.R;
import com.truethat.android.application.App;
import com.truethat.android.common.network.NetworkUtil;
import com.truethat.android.common.network.StudioAPI;
import com.truethat.android.model.Reactable;
import com.truethat.android.ui.common.BaseActivity;
import com.truethat.android.ui.common.media.ReactableFragment;
import com.truethat.android.ui.common.media.ReactableFragmentAdapter;
import com.truethat.android.ui.common.util.OnSwipeTouchListener;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

public class RepertoireActivity extends BaseActivity
    implements ReactableFragment.OnReactableInteractionListener {
  @BindView(R.id.reactablesPager) ViewPager mPager;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  @BindView(R.id.loadingLayout) ViewGroup mLoadingLayout;
  @BindView(R.id.notFoundText) TextView mNotFoundText;
  /**
   * API interface for getting reactables.
   */
  private StudioAPI mStudioAPI;
  private ReactableFragmentAdapter mReactableFragmentAdapter;
  private Callback<List<Reactable>> mFetchReactablesCallback;
  private Call<List<Reactable>> mFetchReactablesCall;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Navigation to other activities
    mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeLeft() {
        if (mPager.getAdapter().getCount() == 0) {
          fetchReactables();
        }
      }

      @Override public void onSwipeDown() {
        startActivity(new Intent(RepertoireActivity.this, StudioActivity.class));
      }
    });
    // Initialize pager
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
    // Initialize API
    mStudioAPI = NetworkUtil.createAPI(StudioAPI.class);
    mFetchReactablesCallback = buildFetchReactablesCallback();
  }

  @Override protected int getLayoutResId() {
    return R.layout.activity_repertoire;
  }

  @Override public void requestDetectionInput() {
    Log.e(TAG, "Input requested by mistake.");
  }

  @Override public void onAuthOk() {
    if (mPager.getAdapter().getCount() == 0) fetchReactables();
  }

  @Override protected void onPause() {
    super.onPause();
    if (mFetchReactablesCall != null) mFetchReactablesCall.cancel();
  }

  /**
   * Fetching User's repertoire from our backend. i.e. the {@link Reactable} of which she is the
   * director.
   */
  public void fetchReactables() {
    Log.v(TAG, "Fetching reactables...");
    if (mPager.getAdapter().getCount() == 0) {
      mLoadingLayout.setVisibility(View.VISIBLE);
      mNotFoundText.setVisibility(GONE);
      Glide.with(this).load(R.drawable.anim_loading_elephant).into(mLoadingImage);
    }
    mFetchReactablesCall = mStudioAPI.getRepertoire(App.getAuthModule().getUser());
    mFetchReactablesCall.enqueue(mFetchReactablesCallback);
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
            RepertoireActivity.this.runOnUiThread(new Runnable() {
              @Override public void run() {
                displayNotFound();
              }
            });
          }
        } else {
          Log.e(TAG, "Failed to get repertoire from "
              + call.request().url()
              + "\n"
              + response.code()
              + " "
              + response.message()
              + "\n"
              + response.headers());
          RepertoireActivity.this.runOnUiThread(new Runnable() {
            @Override public void run() {
              displayNotFound();
            }
          });
        }
      }

      @Override public void onFailure(@NonNull Call<List<Reactable>> call, @NonNull Throwable t) {
        Log.e(TAG, "Get repertoire request to " + call.request().url() + " had failed.", t);
        RepertoireActivity.this.runOnUiThread(new Runnable() {
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
    Glide.with(RepertoireActivity.this).load(R.drawable.sad_teddy).into(mLoadingImage);
  }
}
