package com.truethat.android.view.activity;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.truethat.android.R;
import com.truethat.android.model.Reactable;
import com.truethat.android.view.custom.OnSwipeTouchListener;
import com.truethat.android.view.custom.ReactableFragmentAdapter;
import com.truethat.android.view.fragment.ReactableFragment;
import com.truethat.android.viewmodel.BaseViewModel;
import com.truethat.android.viewmodel.viewinterface.BaseViewInterface;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;

/**
 * Proudly created by ohad on 03/07/2017 for TrueThat.
 */

public abstract class ReactablesPagerActivity<ViewInterface extends BaseViewInterface, ViewModelType extends BaseViewModel<ViewInterface>, DataBinding extends ViewDataBinding>
    extends BaseActivity<ViewInterface, ViewModelType, DataBinding> {
  @BindView(R.id.reactablesPager) ViewPager mPager;
  @BindView(R.id.loadingImage) ImageView mLoadingImage;
  @BindView(R.id.loadingLayout) ViewGroup mLoadingLayout;
  @BindView(R.id.notFoundText) TextView mNotFoundText;
  private ReactableFragmentAdapter mReactableFragmentAdapter;
  private Callback<List<Reactable>> mFetchReactablesCallback;
  private Call<List<Reactable>> mFetchReactablesCall;

  @Override public void onAuthOk() {
    super.onAuthOk();
    if (mPager.getAdapter().getCount() == 0) fetchReactables();
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Navigation to other activities
    mRootView.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeLeft() {
        if (mPager.getAdapter().getCount() == 0) {
          fetchReactables();
        }
      }

      @Override public void onSwipeDown() {
        ReactablesPagerActivity.this.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        ReactablesPagerActivity.this.onSwipeUp();
      }
    });
    mPager.setOnTouchListener(new OnSwipeTouchListener(this) {
      @Override public void onSwipeLeft() {
        // Fetch more reactables if we have none, or if we're at the last item.
        if (mPager.getAdapter().getCount() == 0
            || mPager.getCurrentItem() == mPager.getAdapter().getCount() - 1) {
          fetchReactables();
        } else {
          mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
        }
      }

      @Override public void onSwipeRight() {
        if (mPager.getCurrentItem() != 0) {
          mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
        }
      }

      @Override public void onSwipeDown() {
        ReactablesPagerActivity.this.onSwipeDown();
      }

      @Override public void onSwipeUp() {
        ReactablesPagerActivity.this.onSwipeUp();
      }
    });
    // Initialize views
    mReactableFragmentAdapter = new ReactableFragmentAdapter(getSupportFragmentManager());
    mPager.setAdapter(mReactableFragmentAdapter);
    mFetchReactablesCallback = buildFetchReactablesCallback();
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
    mFetchReactablesCall = buildFetchReactablesCall();
    mFetchReactablesCall.enqueue(mFetchReactablesCallback);
  }

  @VisibleForTesting public ReactableFragment getDisplayedReactable() {
    return (ReactableFragment) mReactableFragmentAdapter.instantiateItem(mPager,
        mPager.getCurrentItem());
  }

  @Override public void onPause() {
    super.onPause();
    if (mFetchReactablesCall != null) mFetchReactablesCall.cancel();
  }

  protected abstract Call<List<Reactable>> buildFetchReactablesCall();

  protected void onSwipeUp() {
  }

  protected void onSwipeDown() {
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
            mReactableFragmentAdapter.append(newReactables);
            mPager.setCurrentItem(toDisplayIndex, true);
          } else if (mPager.getAdapter().getCount() == 0) {
            ReactablesPagerActivity.this.runOnUiThread(new Runnable() {
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
          ReactablesPagerActivity.this.runOnUiThread(new Runnable() {
            @Override public void run() {
              displayNotFound();
            }
          });
        }
      }

      @Override public void onFailure(@NonNull Call<List<Reactable>> call, @NonNull Throwable t) {
        Log.e(TAG, "Fetch reactables request to " + call.request().url() + " had failed.", t);
        ReactablesPagerActivity.this.runOnUiThread(new Runnable() {
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
    Glide.with(ReactablesPagerActivity.this).load(R.drawable.sad_teddy).into(mLoadingImage);
  }
}
