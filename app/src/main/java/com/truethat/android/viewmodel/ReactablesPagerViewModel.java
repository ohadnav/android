package com.truethat.android.viewmodel;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.truethat.android.R;
import com.truethat.android.application.AppContainer;
import com.truethat.android.model.Reactable;
import com.truethat.android.viewmodel.viewinterface.ReactablesPagerViewInterface;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 21/07/2017 for TrueThat.
 */

public class ReactablesPagerViewModel extends BaseFragmentViewModel<ReactablesPagerViewInterface> {
  public final ObservableBoolean mNonFoundTextVisibility = new ObservableBoolean();
  public final ObservableBoolean mLoadingLayoutVisibility = new ObservableBoolean();
  public final ObservableInt mLoadingImageResource = new ObservableInt();
  public final ObservableList<Reactable> mItems = new ObservableArrayList<>();
  private Callback<List<Reactable>> mFetchReactablesCallback;
  private Call<List<Reactable>> mFetchReactablesCall;
  private int mDisplayedIndex;
  private boolean mDetectReactions = false;

  public void setDetectReactions(boolean detectReactions) {
    mDetectReactions = detectReactions;
  }

  public void next() {
    // Fetch more reactables if we have none, or if we're at the last item.
    if (mItems.size() == 0 || mDisplayedIndex == mItems.size() - 1) {
      fetchReactables();
    } else {
      mDisplayedIndex = mDisplayedIndex + 1;
      getView().displayItem(mDisplayedIndex);
    }
  }

  public void previous() {
    if (mDisplayedIndex != 0) {
      mDisplayedIndex = mDisplayedIndex - 1;
      getView().displayItem(mDisplayedIndex);
    }
  }

  @Override public void onStop() {
    super.onStop();
    if (mFetchReactablesCall != null) mFetchReactablesCall.cancel();
    if (mDetectReactions) {
      AppContainer.getReactionDetectionManager().stop();
    }
  }

  @Override public void onStart() {
    super.onStart();
    mFetchReactablesCallback = buildFetchReactablesCallback();
  }

  /**
   * Fetching {@link Reactable} from our backend.
   */
  public void fetchReactables() {
    Log.v(TAG, "Fetching reactables...");
    if (mItems.isEmpty()) {
      mLoadingLayoutVisibility.set(true);
      mNonFoundTextVisibility.set(false);
      mLoadingImageResource.set(R.drawable.anim_loading_elephant);
    }
    mFetchReactablesCall = getView().buildFetchReactablesCall();
    mFetchReactablesCall.enqueue(mFetchReactablesCallback);
  }

  @VisibleForTesting Reactable getDisplayedReactable() {
    if (mDisplayedIndex < 0 || mDisplayedIndex >= mItems.size()) {
      return null;
    }
    return mItems.get(mDisplayedIndex);
  }

  /**
   * @return fetching callback. If new {@link Reactable}s are retrieved, then they are added to
   * {@link #mItems}.
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
            mLoadingLayoutVisibility.set(false);
            // Display new reactables.
            int toDisplayIndex = mItems.size();
            mItems.addAll(newReactables);
            mDisplayedIndex = toDisplayIndex;
            getView().displayItem(mDisplayedIndex);
          } else if (mItems.size() == 0) {
            displayNotFound();
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
          displayNotFound();
        }
      }

      @Override public void onFailure(@NonNull Call<List<Reactable>> call, @NonNull Throwable t) {
        t.printStackTrace();
        Log.e(TAG, "Fetch reactables request to " + call.request().url() + " had failed.", t);
        displayNotFound();
      }
    };
  }

  private void displayNotFound() {
    // Shows not found text
    mLoadingLayoutVisibility.set(true);
    mNonFoundTextVisibility.set(true);
    mLoadingImageResource.set(R.drawable.sad_teddy);
  }
}
