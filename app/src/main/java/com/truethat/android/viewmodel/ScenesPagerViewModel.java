package com.truethat.android.viewmodel;

import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.truethat.android.BuildConfig;
import com.truethat.android.application.AppContainer;
import com.truethat.android.model.Scene;
import com.truethat.android.viewmodel.viewinterface.ScenesPagerViewInterface;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Proudly created by ohad on 21/07/2017 for TrueThat.
 */

public class ScenesPagerViewModel extends BaseFragmentViewModel<ScenesPagerViewInterface> {
  private static final String BUNDLE_ITEMS = "items";
  private static final String BUNDLE_DISPLAYED_INDEX = "displayedIndex";
  public final ObservableBoolean mNonFoundLayoutVisibility = new ObservableBoolean();
  public final ObservableBoolean mLoadingImageVisibility = new ObservableBoolean();
  public final ObservableList<Scene> mItems = new ObservableArrayList<>();
  private Call<List<Scene>> mFetchScenesCall;
  private Integer mDisplayedIndex;
  /**
   * Fetching callback. If new {@link Scene}s are retrieved, then they are added to
   * {@link #mItems}.
   */
  private Callback<List<Scene>> mFetchScenesCallback = new Callback<List<Scene>>() {
    @Override public void onResponse(@NonNull Call<List<Scene>> call,
        @NonNull Response<List<Scene>> response) {
      mLoadingImageVisibility.set(false);
      if (response.isSuccessful()) {
        List<Scene> fetchedScenes = response.body();
        if (fetchedScenes == null) {
          throw new AssertionError("I just cant believe it! The new scenes are null.");
        }
        // Find new scenes, to avoid duplicate scenes
        List<Scene> newScenes = new ArrayList<>();
        for (Scene fetchedScene : fetchedScenes) {
          boolean isNew = true;
          for (Scene existingScene : mItems) {
            if (Objects.equals(existingScene.getId(), fetchedScene.getId())) {
              isNew = false;
            }
          }
          for (Scene alreadyConsidered : newScenes) {
            if (Objects.equals(alreadyConsidered.getId(), fetchedScene.getId())) {
              isNew = false;
            }
          }
          if (isNew) newScenes.add(fetchedScene);
        }
        if (newScenes.size() > 0) {
          Log.v(TAG, "Loading " + newScenes.size() + " new scenes.");
          // Hides the loading image.
          mLoadingImageVisibility.set(false);
          // Display new scenes.
          int toDisplayIndex = mItems.size();
          mItems.addAll(newScenes);
          mDisplayedIndex = toDisplayIndex;
          if (getView() != null) {
            getView().displayItem(mDisplayedIndex);
          }
        } else if (mItems.size() == 0) {
          displayNotFound();
        }
      } else {
        if (!BuildConfig.DEBUG) {
          Crashlytics.logException(new Exception("Failed to fetch scenes"));
        }
        Log.e(TAG, "Failed to fetch scenes from "
            + call.request().url()
            + "\nUser: "
            + AppContainer.getAuthManager().getCurrentUser()
            + "\nResponse: "
            + response.code()
            + " "
            + response.message()
            + "\n"
            + response.headers());
        displayNotFound();
      }
    }

    @Override public void onFailure(@NonNull Call<List<Scene>> call, @NonNull Throwable t) {
      if (!BuildConfig.DEBUG) {
        Crashlytics.logException(t);
      }
      t.printStackTrace();
      Log.e(TAG, "Fetch scenes request to " + call.request().url() + " had failed.", t);
      displayNotFound();
    }
  };

  public void next() {
    // Fetch more scenes if we have none, or if we're at the last item.
    if (mItems.size() == 0 || mDisplayedIndex == mItems.size() - 1) {
      fetchScenes();
    } else {
      mDisplayedIndex = mDisplayedIndex + 1;
      if (getView() != null) {
        getView().vibrate();
        getView().displayItem(mDisplayedIndex);
      }
    }
  }

  public void previous() {
    if (mDisplayedIndex != null && mDisplayedIndex != 0) {
      mDisplayedIndex = mDisplayedIndex - 1;
      if (getView() != null) {
        getView().vibrate();
        getView().displayItem(mDisplayedIndex);
      }
    }
  }

  @Override public void onCreate(@Nullable Bundle arguments, @Nullable Bundle savedInstanceState) {
    super.onCreate(arguments, savedInstanceState);
    if (savedInstanceState != null) {
      if (savedInstanceState.getParcelableArrayList(BUNDLE_ITEMS) != null) {
        //noinspection StatementWithEmptyBody
        while (!mItems.isEmpty()) {
          mItems.remove(0);
        }
        ArrayList<Parcelable> savedItems = savedInstanceState.getParcelableArrayList(BUNDLE_ITEMS);
        if (savedItems != null) {
          for (Parcelable savedItem : savedItems) {
            mItems.add((Scene) savedItem);
          }
        }
      }
      if (savedInstanceState.get(BUNDLE_DISPLAYED_INDEX) != null) {
        mDisplayedIndex = savedInstanceState.getInt(BUNDLE_DISPLAYED_INDEX);
      }
    }
  }

  @Override public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (!mItems.isEmpty()) {
      outState.putParcelableArrayList(BUNDLE_ITEMS, new ArrayList<>(mItems));
    }
    if (mDisplayedIndex != null) {
      outState.putInt(BUNDLE_DISPLAYED_INDEX, mDisplayedIndex);
    }
  }

  @Override public void onVisible() {
    super.onVisible();
    fetchScenes();
    if (mDisplayedIndex != null && !mItems.isEmpty() && getView() != null) {
      getView().displayItem(mDisplayedIndex);
    }
  }

  @Override public void onHidden() {
    super.onHidden();
    if (mFetchScenesCall != null) {
      mFetchScenesCall.cancel();
    }
    AppContainer.getReactionDetectionManager().stop();
  }

  @VisibleForTesting Scene getDisplayedScene() {
    if (mDisplayedIndex == null || mDisplayedIndex < 0 || mDisplayedIndex >= mItems.size()) {
      return null;
    }
    return mItems.get(mDisplayedIndex);
  }

  /**
   * Fetching {@link Scene} from our backend.
   */
  private void fetchScenes() {
    Log.d(TAG, "Fetching scenes...");
    mNonFoundLayoutVisibility.set(false);
    if (mItems.isEmpty()) {
      mLoadingImageVisibility.set(true);
    }
    if (getView() != null) {
      mFetchScenesCall = getView().buildFetchScenesCall();
      mFetchScenesCall.enqueue(mFetchScenesCallback);
    }
  }

  private void displayNotFound() {
    // Shows not found text
    mNonFoundLayoutVisibility.set(true);
  }
}
